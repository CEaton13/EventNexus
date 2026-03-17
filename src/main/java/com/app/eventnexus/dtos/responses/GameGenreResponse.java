package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.BackgroundStyle;
import com.app.eventnexus.models.GameGenre;

/**
 * Response DTO for a game genre.
 * Carries the full theme palette consumed by the frontend {@code ThemeService}.
 */
public class GameGenreResponse {

    private Long id;
    private String name;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private BackgroundStyle backgroundStyle;
    private String fontFamily;
    private String iconPackKey;
    private String heroImageUrl;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code GameGenreResponse} from a {@link GameGenre} entity.
     *
     * @param genre the source entity
     * @return a populated response DTO
     */
    public static GameGenreResponse from(GameGenre genre) {
        GameGenreResponse dto = new GameGenreResponse();
        dto.id = genre.getId();
        dto.name = genre.getName();
        dto.primaryColor = genre.getPrimaryColor();
        dto.secondaryColor = genre.getSecondaryColor();
        dto.accentColor = genre.getAccentColor();
        dto.backgroundStyle = genre.getBackgroundStyle();
        dto.fontFamily = genre.getFontFamily();
        dto.iconPackKey = genre.getIconPackKey();
        dto.heroImageUrl = genre.getHeroImageUrl();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public GameGenreResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
