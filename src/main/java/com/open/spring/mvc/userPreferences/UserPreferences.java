package com.open.spring.mvc.userPreferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserPreferences entity stores user styling and accessibility preferences.
 * This entity has a one-to-one relationship with the Person entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id", unique = true)
    @JsonIgnore
    private Person person;

    // Styling preferences
    @Column(length = 20)
    private String backgroundColor = "#0b1220";

    @Column(length = 20)
    private String textColor = "#e6eef8";

    @Column(length = 255)
    private String fontFamily = "Inter, system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial";

    private Integer fontSize = 14;

    @Column(length = 20)
    private String accentColor = "#3b82f6";

    @Column(length = 20)
    private String selectionColor = "#3b82f6";

    @Column(length = 20)
    private String buttonStyle = "rounded";

    // Language preference
    @Column(length = 10)
    private String language = "";

    // Text-to-Speech preferences
    @Column(length = 100)
    private String ttsVoice = "";

    private Double ttsRate = 1.0;

    private Double ttsPitch = 1.0;

    private Double ttsVolume = 1.0;

    // Custom themes stored as JSON string
    @Column(columnDefinition = "TEXT")
    private String customThemes = "{}";

    /**
     * Constructor to create UserPreferences for a specific Person
     * @param person The Person entity this preferences belongs to
     */
    public UserPreferences(Person person) {
        this.person = person;
    }

    /**
     * Creates a default UserPreferences object (without Person association)
     * Used for returning default preferences when user has no saved preferences
     * @return UserPreferences with default values
     */
    public static UserPreferences getDefaultPreferences() {
        UserPreferences defaults = new UserPreferences();
        defaults.setBackgroundColor("#0b1220");
        defaults.setTextColor("#e6eef8");
        defaults.setFontFamily("Inter, system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial");
        defaults.setFontSize(14);
        defaults.setAccentColor("#3b82f6");
        defaults.setSelectionColor("#3b82f6");
        defaults.setButtonStyle("rounded");
        defaults.setLanguage("");
        defaults.setTtsVoice("");
        defaults.setTtsRate(1.0);
        defaults.setTtsPitch(1.0);
        defaults.setTtsVolume(1.0);
        defaults.setCustomThemes("{}");
        return defaults;
    }
}
