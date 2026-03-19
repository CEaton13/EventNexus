package com.app.eventnexus.dtos.requests;

/**
 * Request DTO for creating a new organization.
 */
public class CreateOrganizationRequest {

    private String name;
    private String slug;
    private String contactEmail;

    public CreateOrganizationRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
