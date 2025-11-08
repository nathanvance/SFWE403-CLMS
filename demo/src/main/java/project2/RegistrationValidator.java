package project2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Shared validation logic for newly registered users.
 */
public final class RegistrationValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z ,.'-]{2,}$");

    private RegistrationValidator() {
    }

    public static List<String> validate(RegistrationProfile profile) {
        List<String> errors = new ArrayList<>();
        if (profile == null) {
            errors.add("Registration details are required.");
            return errors;
        }

        if (isBlank(profile.getUsername())) {
            errors.add("Username is required.");
        }

        if (isBlank(profile.getFullName()) || !NAME_PATTERN.matcher(profile.getFullName()).matches()) {
            errors.add("Full name must contain only alphabetic characters and punctuation.");
        }

        if (!isValidDate(profile.getDateOfBirth())) {
            errors.add("Date of birth must be in YYYY-MM-DD format.");
        }

        if (isBlank(profile.getEmail()) || !EMAIL_PATTERN.matcher(profile.getEmail()).matches()) {
            errors.add("Email address is invalid.");
        }

        String digitsOnly = profile.getDigitsOnlyPhone();
        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            errors.add("Phone number must contain 10-15 digits.");
        }

        return errors;
    }

    private static boolean isValidDate(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
