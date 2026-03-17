package com.app.eventnexus.dtos.requests;

/**
 * Request body for creating or updating a team.
 */
public class TeamRequest {

    private String name;
    private String tag;
    private String logoUrl;
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
