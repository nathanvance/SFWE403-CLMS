package project2;

import java.util.Locale;

/**
 * Immutable bundle of additional data collected during user registration.
 */
public final class RegistrationProfile {
    private final String username;
    private final String fullName;
    private final String dateOfBirth;
    private final String email;
    private final String phone;

    public RegistrationProfile(String username,
                               String fullName,
                               String dateOfBirth,
                               String email,
                               String phone) {
        this.username = sanitize(username);
        this.fullName = sanitize(fullName);
        this.dateOfBirth = safe(dateOfBirth);
        this.email = safe(email);
        this.phone = safe(phone);
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getNormalizedEmail() {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getDigitsOnlyPhone() {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }

    public String toCsvRow() {
        return String.join(",",
                sanitize(username),
                sanitize(fullName),
                dateOfBirth == null ? "" : dateOfBirth.trim(),
                getNormalizedEmail(),
                getDigitsOnlyPhone()
        );
    }

    public RegistrationProfile withUsername(String newUsername) {
        return new RegistrationProfile(newUsername, fullName, dateOfBirth, email, phone);
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ").trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
