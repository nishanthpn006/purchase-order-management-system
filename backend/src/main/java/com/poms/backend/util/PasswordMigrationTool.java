package com.poms.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Utility to migrate user passwords in PostgreSQL from plaintext to BCrypt hashes.
 *
 * This tool:
 * - Is invoked explicitly and does NOT run on application startup.
 * - Supports --dry-run (default if --confirm is omitted).
 * - Requires --confirm to commit changes.
 * - Operates in an atomic database transaction.
 * - Automatically skips passwords that already match standard BCrypt format.
 * - Never prints plaintext passwords, hashes, or database credentials.
 */
public class PasswordMigrationTool {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[A-Za-z0-9./]{53}$");

    public static void main(String[] args) {
        boolean confirm = false;
        boolean dryRun = false;

        for (String arg : args) {
            if ("--confirm".equalsIgnoreCase(arg)) {
                confirm = true;
            } else if ("--dry-run".equalsIgnoreCase(arg)) {
                dryRun = true;
            } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printUsage();
                return;
            }
        }

        if (confirm && dryRun) {
            System.err.println("Error: Cannot specify both --confirm and --dry-run.");
            System.exit(1);
        }

        boolean executeWrite = confirm;

        System.out.println("=== POMS Password Migration Tool ===");
        System.out.println("Mode: " + (executeWrite ? "LIVE MIGRATION (--confirm)" : "DRY RUN (read-only)"));

        DbConfig config = loadDbConfig();
        if (config == null || config.url == null || config.user == null) {
            System.err.println("Error: Unable to resolve database configuration.");
            System.exit(1);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try (Connection conn = DriverManager.getConnection(config.url, config.user, config.password)) {
            conn.setAutoCommit(false);

            int totalRows = 0;
            int alreadyHashed = 0;
            int toMigrate = 0;

            String selectSql = "SELECT id, password FROM users";
            String updateSql = "UPDATE users SET password = ? WHERE id = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery();
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                while (rs.next()) {
                    totalRows++;
                    long id = rs.getLong("id");
                    String currentPassword = rs.getString("password");

                    if (currentPassword != null && BCRYPT_PATTERN.matcher(currentPassword).matches()) {
                        alreadyHashed++;
                    } else if (currentPassword != null && !currentPassword.isBlank()) {
                        toMigrate++;
                        String encoded = encoder.encode(currentPassword);
                        updateStmt.setString(1, encoded);
                        updateStmt.setLong(2, id);
                        updateStmt.addBatch();
                    }
                }

                System.out.println("------------------------------------");
                System.out.println("Total user accounts inspected: " + totalRows);
                System.out.println("Already formatted with BCrypt: " + alreadyHashed);
                System.out.println((executeWrite ? "Migrated to BCrypt:            " : "Identified for BCrypt migration: ") + toMigrate);
                System.out.println("------------------------------------");

                if (toMigrate > 0) {
                    if (executeWrite) {
                        updateStmt.executeBatch();
                        conn.commit();
                        System.out.println("SUCCESS: Transaction committed. All passwords updated to BCrypt.");
                    } else {
                        conn.rollback();
                        System.out.println("DRY RUN COMPLETE: Database unchanged (transaction rolled back).");
                        System.out.println("To execute and commit these changes, run with: --confirm");
                    }
                } else {
                    conn.rollback();
                    System.out.println("No passwords require migration.");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Database operation failed: " + e.getMessage());
                System.exit(1);
            }
        } catch (SQLException e) {
            System.err.println("Could not connect to database: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java PasswordMigrationTool [--dry-run | --confirm]");
        System.out.println("  --dry-run   (Default) Inspect database and preview migration counts without writing.");
        System.out.println("  --confirm   Perform the password hashing updates and commit the transaction.");
    }

    private static DbConfig loadDbConfig() {
        DbConfig config = new DbConfig();

        // 1. Environment variables and system properties
        String envUrl = getEnvOrProperty("SPRING_DATASOURCE_URL");
        if (envUrl == null) envUrl = getEnvOrProperty("spring.datasource.url");
        if (envUrl == null) envUrl = getEnvOrProperty("DB_URL");

        String envUser = getEnvOrProperty("SPRING_DATASOURCE_USERNAME");
        if (envUser == null) envUser = getEnvOrProperty("spring.datasource.username");
        if (envUser == null) envUser = getEnvOrProperty("DB_USER");
        if (envUser == null) envUser = getEnvOrProperty("PGUSER");

        String envPass = getEnvOrProperty("SPRING_DATASOURCE_PASSWORD");
        if (envPass == null) envPass = getEnvOrProperty("spring.datasource.password");
        if (envPass == null) envPass = getEnvOrProperty("DB_PASSWORD");
        if (envPass == null) envPass = getEnvOrProperty("PGPASSWORD");

        // 2. application.properties file
        Properties props = new Properties();
        try (InputStream in = PasswordMigrationTool.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
        }

        // Also check filesystem fallback if classpath didn't find it
        if (props.isEmpty()) {
            File propFile = new File("src/main/resources/application.properties");
            if (!propFile.exists()) {
                propFile = new File("backend/src/main/resources/application.properties");
            }
            if (propFile.exists()) {
                try (InputStream in = new FileInputStream(propFile)) {
                    props.load(in);
                } catch (Exception ignored) {
                }
            }
        }

        config.url = (envUrl != null) ? envUrl : resolvePlaceholder(props.getProperty("spring.datasource.url"));
        config.user = (envUser != null) ? envUser : resolvePlaceholder(props.getProperty("spring.datasource.username"));
        config.password = (envPass != null) ? envPass : resolvePlaceholder(props.getProperty("spring.datasource.password"));

        return config;
    }

    private static String getEnvOrProperty(String key) {
        if (key == null) return null;
        String val = System.getProperty(key);
        if (val != null && !val.isBlank()) {
            return val;
        }
        val = System.getenv(key);
        if (val != null && !val.isBlank()) {
            return val;
        }
        return null;
    }

    private static String resolvePlaceholder(String rawValue) {
        if (rawValue == null) return null;
        String val = rawValue.trim();
        if (val.startsWith("${") && val.endsWith("}")) {
            String inner = val.substring(2, val.length() - 1);
            int colonIndex = inner.indexOf(':');
            String varName = (colonIndex != -1) ? inner.substring(0, colonIndex).trim() : inner.trim();
            String defaultValue = (colonIndex != -1) ? inner.substring(colonIndex + 1).trim() : null;

            if (defaultValue != null && defaultValue.startsWith("${") && defaultValue.endsWith("}")) {
                defaultValue = resolvePlaceholder(defaultValue);
            }

            String resolved = getEnvOrProperty(varName);
            if (resolved == null) {
                if (varName.contains("_")) {
                    resolved = getEnvOrProperty(varName.toLowerCase().replace('_', '.'));
                } else if (varName.contains(".")) {
                    resolved = getEnvOrProperty(varName.toUpperCase().replace('.', '_'));
                }
            }

            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
            return defaultValue;
        }
        return val;
    }

    private static class DbConfig {
        String url;
        String user;
        String password;
    }
}
