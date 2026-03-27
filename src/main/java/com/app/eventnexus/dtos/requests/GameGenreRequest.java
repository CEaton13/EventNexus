package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.BackgroundStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a game genre.
 */
public class GameGenreRequest {

    @NotBlank(message = "Genre name is required")
    @Size(max = 100, message = "Genre name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Primary color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hex color (e.g. #FF5500)")
    private String primaryColor;

    @NotBlank(message = "Secondary color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Secondary color must be a valid hex color")
    private String secondaryColor;

    @NotBlank(message = "Accent color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Accent color must be a valid hex color")
    private String accentColor;

    @NotNull(message = "Background style is required")
    private BackgroundStyle backgroundStyle;

    @NotBlank(message = "Font family is required")
    @Size(max = 100, message = "Font family must not exceed 100 characters")
    private String fontFamily;

    @NotBlank(message = "Icon pack key is required")
    @Size(max = 50, message = "Icon pack key must not exceed 50 characters")
    private String iconPackKey;

    @Size(max = 500, message = "Hero image URL must not exceed 500 characters")
    private String heroImageUrl;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public GameGenreRequest() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public BackgroundStyle getBackgroundStyle() {
        return backgroundStyle;
    }

    public void setBackgroundStyle(BackgroundStyle backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getIconPackKey() {
        return iconPackKey;
    }

    public void setIconPackKey(String iconPackKey) {
        this.iconPackKey = iconPackKey;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }
}
