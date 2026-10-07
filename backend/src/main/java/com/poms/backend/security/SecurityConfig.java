package com.poms.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtFilter jwtFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                return bCrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                    return bCrypt.matches(rawPassword, encodedPassword);
                }
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Apply our CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // IMPORTANT: In Spring Security 7, you must use AbstractHttpConfigurer::disable
            // to properly disable CSRF. The lambda csrf -> csrf.disable() does NOT work.
            // CSRF must be disabled for stateless REST APIs that use JWT.
            .csrf(AbstractHttpConfigurer::disable)

            // Return 401 Unauthorized for unauthenticated requests or invalid tokens
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
            )

            // Stateless sessions — no HTTP session is created or used
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoint: POST /api/login requires no token
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                // Public endpoint: GET /api/health requires no token
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers("/error").permitAll()
                // Public Swagger & OpenAPI endpoints
                .requestMatchers(
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Status update restricted to Admin and Manager roles
                .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/*/status")
                    .hasAnyRole("ADMIN", "MANAGER")
                // Vendor creation restricted to Admin and Manager roles
                .requestMatchers(HttpMethod.POST, "/api/vendors")
                    .hasAnyRole("ADMIN", "MANAGER")
                // Product creation restricted to Admin and Manager roles
                .requestMatchers(HttpMethod.POST, "/api/products")
                    .hasAnyRole("ADMIN", "MANAGER")
                // Product update restricted to Admin and Manager roles
                .requestMatchers(HttpMethod.PUT, "/api/products/*", "/api/products/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                // Product deactivation restricted to Admin and Manager roles
                .requestMatchers(HttpMethod.PATCH, "/api/products/*/deactivate")
                    .hasAnyRole("ADMIN", "MANAGER")
                // All other endpoints require a valid JWT
                .anyRequest().authenticated()
            )

            // Use our custom DaoAuthenticationProvider (BCrypt + UserDetailsService)
            .authenticationProvider(authenticationProvider())

            // Run our JWT filter before the default username/password auth filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
