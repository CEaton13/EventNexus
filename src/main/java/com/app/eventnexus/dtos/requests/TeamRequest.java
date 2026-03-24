package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a team.
 */
public class TeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Team tag is required")
    @Size(max = 10, message = "Team tag must not exceed 10 characters")
    private String tag;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 100, message = "Home region must not exceed 100 characters")
    private String homeRegion;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TeamRequest() {
    }

    public TeamRequest(String name, String tag, String logoUrl, String homeRegion) {
        this.name = name;
        this.tag = tag;
        this.logoUrl = logoUrl;
        this.homeRegion = homeRegion;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getHomeRegion() {
        return homeRegion;
    }

    public void setHomeRegion(String homeRegion) {
        this.homeRegion = homeRegion;
    }
}
