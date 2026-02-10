package com.mymicroservice.authservice.advice;

import com.mymicroservice.authservice.exception.InvalidCredentialsException;
import com.mymicroservice.authservice.exception.UserCredentialNotFoundException;
import com.mymicroservice.authservice.util.ErrorItem;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalAdvice {

    /**
     * Handles validation exceptions for DTO fields when controller method parameters
     * annotated with @Valid fail validation, such as @NotNull, @NotBlank, @Size, @Email, etc.
     *
     * <p>This method extracts field-specific error messages and returns them
     * in the `fieldErrors` map, where keys are field names and values are messages.
     * It also returns a general message, timestamp, URL, and HTTP 400 status code.
     *
     * @param e the MethodArgumentNotValidException containing validation error information
     * @return ResponseEntity containing an ErrorItem object with:
     *         - general message ("Validation failed")
     *         - map of fieldErrors (field name → validation message)
     *         - timestamp
     *         - request URL
     *         - HTTP 400 status code (BAD_REQUEST)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorItem> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorItem error = ErrorItem.fromMethodArgumentNotValid(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorItem> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    public ResponseEntity<ErrorItem> handleInvalidCredentialsException(InvalidCredentialsException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    /**
     * Handles data integrity violation exceptions, for example,
     * when attempting to save a duplicate unique field (such as email),
     * NOT NULL constraint violations and etc.
     *
     * @param e the DataIntegrityViolationException to handle
     * @return ResponseEntity containing error information with BAD_REQUEST status
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorItem> handleBadCredentialsException(DataIntegrityViolationException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    /**
     * Handles {@link HttpMessageNotReadableException} which occurs when HTTP request body
     * cannot be properly parsed or converted to the expected Java object.
     *
     * <p>This typically happens when:
     * <ul>
     *   <li>Malformed JSON syntax in request body</li>
     *   <li>Type mismatch between JSON values and target Java types</li>
     *   <li>Invalid enum values that cannot be converted to the target enum type</li>
     *   <li>Missing required fields in JSON payload</li>
     * </ul>
     *
     * @param e the HttpMessageNotReadableException that was thrown during request processing
     * @return ResponseEntity containing ErrorItem with details about the parsing error
     * @see org.springframework.http.converter.HttpMessageNotReadableException
     * @see org.springframework.http.HttpStatus#BAD_REQUEST
     * @since 1.0
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorItem> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<ErrorItem> handleUsernameNotFoundException(UsernameNotFoundException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    @ExceptionHandler({UserCredentialNotFoundException.class})
    public ResponseEntity<ErrorItem> handleUserCredentialNotFoundException(UserCredentialNotFoundException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorItem> handleEntityNotFoundException(EntityNotFoundException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }
}