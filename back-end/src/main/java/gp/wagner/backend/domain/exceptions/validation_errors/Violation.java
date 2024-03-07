package gp.wagner.backend.domain.exceptions.validation_errors;


public record Violation(String fieldName, String message) {
}
