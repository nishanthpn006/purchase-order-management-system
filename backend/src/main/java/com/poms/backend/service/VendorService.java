package com.poms.backend.service;

import com.poms.backend.entity.Vendor;
import com.poms.backend.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Optional<Vendor> getVendorById(Integer id) {
        return vendorRepository.findById(id);
    }

    public Vendor saveVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Vendor createVendor(com.poms.backend.dto.CreateVendorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (request.getVendorName() == null || request.getVendorName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor name is required");
        }

        String vendorName = request.getVendorName().trim();
        if (vendorName.length() > 150) {
            throw new IllegalArgumentException("Vendor name must not exceed 150 characters");
        }

        String contactPerson = null;
        if (request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) {
            contactPerson = request.getContactPerson().trim();
            if (contactPerson.length() > 100) {
                throw new IllegalArgumentException("Contact person must not exceed 100 characters");
            }
        }

        String email = null;
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            email = request.getEmail().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (email.length() > 100) {
                throw new IllegalArgumentException("Email must not exceed 100 characters");
            }
        }

        String phone = null;
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            phone = request.getPhone().trim();
            if (phone.length() > 20) {
                throw new IllegalArgumentException("Phone number must not exceed 20 characters");
            }
        }

        String address = null;
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            address = request.getAddress().trim();
        }

        String gstNumber = null;
        if (request.getGstNumber() != null && !request.getGstNumber().trim().isEmpty()) {
            gstNumber = request.getGstNumber().trim();
            if (gstNumber.length() > 30) {
                throw new IllegalArgumentException("GST number must not exceed 30 characters");
            }
        }

        String status = "Active";
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String rawStatus = request.getStatus().trim();
            if (!rawStatus.equalsIgnoreCase("Active") && !rawStatus.equalsIgnoreCase("Inactive")) {
                throw new IllegalArgumentException("Status must be either 'Active' or 'Inactive'");
            }
            status = rawStatus.equalsIgnoreCase("Active") ? "Active" : "Inactive";
        }

        Vendor vendor = new Vendor();
        vendor.setVendorName(vendorName);
        vendor.setContactPerson(contactPerson);
        vendor.setEmail(email);
        vendor.setPhone(phone);
        vendor.setAddress(address);
        vendor.setGstNumber(gstNumber);
        vendor.setStatus(status);
        vendor.setCreatedAt(java.time.LocalDateTime.now());

        return vendorRepository.save(vendor);
    }
}