package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for updating an existing vendor record.
 * PUT /api/vendors/{id}
 */
@Schema(description = "Vendor update payload including contact, tax registration, and status information")
public class UpdateVendorRequest {

    @Schema(description = "Full legal name of the vendor/supplier", example = "Dell Technologies", requiredMode = Schema.RequiredMode.REQUIRED)
    private String vendorName;

    @Schema(description = "Primary point of contact person", example = "Arun Patel")
    private String contactPerson;

    @Schema(description = "Official communication email address", example = "dell@poms.com")
    private String email;

    @Schema(description = "Primary contact telephone or mobile number", example = "9876543210")
    private String phone;

    @Schema(description = "Physical or registered office address", example = "Bangalore, Karnataka")
    private String address;

    @Schema(description = "GST identification or tax registration number", example = "29ABCDE1234F1Z5")
    private String gstNumber;

    @Schema(description = "Vendor status", example = "Active", allowableValues = {"Active", "Inactive"})
    private String status;

    public UpdateVendorRequest() {
    }

    public UpdateVendorRequest(String vendorName, String contactPerson, String email, String phone, String address, String gstNumber, String status) {
        this.vendorName = vendorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.gstNumber = gstNumber;
        this.status = status;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
