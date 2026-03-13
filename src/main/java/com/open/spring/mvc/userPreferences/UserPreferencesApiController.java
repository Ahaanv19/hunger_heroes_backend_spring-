package com.open.spring.mvc.userPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import lombok.Getter;
import lombok.Setter;

/**
 * REST API Controller for managing User Preferences.
 * All endpoints require JWT authentication.
 * Users can only access/modify their own preferences.
 */
@RestController
@RequestMapping("/api/user")
public class UserPreferencesApiController {

    @Autowired
    private UserPreferencesJpaRepository preferencesRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    /**
     * DTO for receiving preference updates from the frontend
     */
    @Getter
    @Setter
    public static class PreferencesDto {
        private String backgroundColor;
        private String textColor;
        private String fontFamily;
        private Integer fontSize;
        private String accentColor;
        private String selectionColor;
        private String buttonStyle;
        private String language;
        private String ttsVoice;
        private Double ttsRate;
        private Double ttsPitch;
        private Double ttsVolume;
        private String customThemes;
    }

    /**
     * GET /api/user/preferences
     * Returns the logged-in user's preferences.
     * If no preferences exist, returns default preferences (without creating them).
     * 
     * @param userDetails The authenticated user details from JWT
     * @return ResponseEntity containing user preferences or defaults
     */
    @GetMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        // Check if user is authenticated
        if (userDetails == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not authenticated"),
                HttpStatus.UNAUTHORIZED
            );
        }

        // Find the person by username (uid)
        Person person = personRepository.findByUid(userDetails.getUsername());
        if (person == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not found"),
                HttpStatus.NOT_FOUND
            );
        }

        // Try to find existing preferences
        Optional<UserPreferences> existingPreferences = preferencesRepository.findByPerson(person);

        if (existingPreferences.isPresent()) {
            // Return existing preferences
            return new ResponseEntity<>(
                preferencesToMap(existingPreferences.get()),
                HttpStatus.OK
            );
        } else {
            // Return default preferences (without creating them)
            return new ResponseEntity<>(
                preferencesToMap(UserPreferences.getDefaultPreferences()),
                HttpStatus.OK
            );
        }
    }

    /**
     * POST /api/user/preferences
     * Creates preferences for the logged-in user (if they don't exist).
     * 
     * @param userDetails The authenticated user details from JWT
     * @param preferencesDto The preferences data to create
     * @return ResponseEntity containing created preferences
     */
    @PostMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> createPreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PreferencesDto preferencesDto) {
        
        // Check if user is authenticated
        if (userDetails == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not authenticated"),
                HttpStatus.UNAUTHORIZED
            );
        }

        // Find the person by username (uid)
        Person person = personRepository.findByUid(userDetails.getUsername());
        if (person == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not found"),
                HttpStatus.NOT_FOUND
            );
        }

        // Check if preferences already exist
        if (preferencesRepository.existsByPerson(person)) {
            return new ResponseEntity<>(
                Map.of("error", "Preferences already exist. Use PUT to update."),
                HttpStatus.CONFLICT
            );
        }

        // Create new preferences
        UserPreferences preferences = new UserPreferences(person);
        updatePreferencesFromDto(preferences, preferencesDto);

        // Save and return
        UserPreferences savedPreferences = preferencesRepository.save(preferences);
        return new ResponseEntity<>(
            preferencesToMap(savedPreferences),
            HttpStatus.CREATED
        );
    }

    /**
     * PUT /api/user/preferences
     * Updates the logged-in user's preferences.
     * Creates preferences if they don't exist.
     * 
     * @param userDetails The authenticated user details from JWT
     * @param preferencesDto The preferences data to update
     * @return ResponseEntity containing updated preferences
     */
    @PutMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PreferencesDto preferencesDto) {
        
        // Check if user is authenticated
        if (userDetails == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not authenticated"),
                HttpStatus.UNAUTHORIZED
            );
        }

        // Find the person by username (uid)
        Person person = personRepository.findByUid(userDetails.getUsername());
        if (person == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not found"),
                HttpStatus.NOT_FOUND
            );
        }

        // Find existing preferences or create new ones
        UserPreferences preferences = preferencesRepository.findByPerson(person)
            .orElseGet(() -> new UserPreferences(person));

        // Update preferences from DTO
        updatePreferencesFromDto(preferences, preferencesDto);

        // Save and return
        UserPreferences savedPreferences = preferencesRepository.save(preferences);
        return new ResponseEntity<>(
            preferencesToMap(savedPreferences),
            HttpStatus.OK
        );
    }

    /**
     * DELETE /api/user/preferences
     * Deletes/resets the logged-in user's preferences.
     * 
     * @param userDetails The authenticated user details from JWT
     * @return ResponseEntity with success message
     */
    @DeleteMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> deletePreferences(@AuthenticationPrincipal UserDetails userDetails) {
        // Check if user is authenticated
        if (userDetails == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not authenticated"),
                HttpStatus.UNAUTHORIZED
            );
        }

        // Find the person by username (uid)
        Person person = personRepository.findByUid(userDetails.getUsername());
        if (person == null) {
            return new ResponseEntity<>(
                Map.of("error", "User not found"),
                HttpStatus.NOT_FOUND
            );
        }

        // Find and delete existing preferences
        Optional<UserPreferences> existingPreferences = preferencesRepository.findByPerson(person);
        
        if (existingPreferences.isPresent()) {
            preferencesRepository.delete(existingPreferences.get());
            return new ResponseEntity<>(
                Map.of("message", "Preferences deleted successfully"),
                HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                Map.of("message", "No preferences found to delete"),
                HttpStatus.OK
            );
        }
    }

    /**
     * Helper method to update preferences from DTO.
     * Only updates fields that are not null in the DTO.
     * 
     * @param preferences The preferences entity to update
     * @param dto The DTO containing new values
     */
    private void updatePreferencesFromDto(UserPreferences preferences, PreferencesDto dto) {
        if (dto.getBackgroundColor() != null) {
            preferences.setBackgroundColor(dto.getBackgroundColor());
        }
        if (dto.getTextColor() != null) {
            preferences.setTextColor(dto.getTextColor());
        }
        if (dto.getFontFamily() != null) {
            preferences.setFontFamily(dto.getFontFamily());
        }
        if (dto.getFontSize() != null) {
            preferences.setFontSize(dto.getFontSize());
        }
        if (dto.getAccentColor() != null) {
            preferences.setAccentColor(dto.getAccentColor());
        }
        if (dto.getSelectionColor() != null) {
            preferences.setSelectionColor(dto.getSelectionColor());
        }
        if (dto.getButtonStyle() != null) {
            preferences.setButtonStyle(dto.getButtonStyle());
        }
        if (dto.getLanguage() != null) {
            preferences.setLanguage(dto.getLanguage());
        }
        if (dto.getTtsVoice() != null) {
            preferences.setTtsVoice(dto.getTtsVoice());
        }
        if (dto.getTtsRate() != null) {
            preferences.setTtsRate(dto.getTtsRate());
        }
        if (dto.getTtsPitch() != null) {
            preferences.setTtsPitch(dto.getTtsPitch());
        }
        if (dto.getTtsVolume() != null) {
            preferences.setTtsVolume(dto.getTtsVolume());
        }
        if (dto.getCustomThemes() != null) {
            preferences.setCustomThemes(dto.getCustomThemes());
        }
    }

    /**
     * Helper method to convert preferences entity to a Map for JSON response.
     * This ensures consistent response format.
     * 
     * @param preferences The preferences entity
     * @return Map containing all preference fields
     */
    private Map<String, Object> preferencesToMap(UserPreferences preferences) {
        Map<String, Object> map = new HashMap<>();
        
        if (preferences.getId() != null) {
            map.put("id", preferences.getId());
        }
        map.put("backgroundColor", preferences.getBackgroundColor());
        map.put("textColor", preferences.getTextColor());
        map.put("fontFamily", preferences.getFontFamily());
        map.put("fontSize", preferences.getFontSize());
        map.put("accentColor", preferences.getAccentColor());
        map.put("selectionColor", preferences.getSelectionColor());
        map.put("buttonStyle", preferences.getButtonStyle());
        map.put("language", preferences.getLanguage());
        map.put("ttsVoice", preferences.getTtsVoice());
        map.put("ttsRate", preferences.getTtsRate());
        map.put("ttsPitch", preferences.getTtsPitch());
        map.put("ttsVolume", preferences.getTtsVolume());
        map.put("customThemes", preferences.getCustomThemes());
        
        return map;
    }
}
