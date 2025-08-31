package com.mymicroservice.authservice.advice;

import com.mymicroservice.authservice.exception.InvalidCredentialsException;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalAdvice {

    /**
     * Handles validation exceptions for DTO fields when data fails validation annotations
     * such as @Valid, @NotNull, @Size, @Pattern and others.
     *
     * @param e MethodArgumentNotValidException containing validation error information
     * @return ResponseEntity with an ErrorItem object containing:
     *         - List of error messages
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 400 status (BAD_REQUEST)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorItem> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorItem error = new ErrorItem();
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList())
                .toString();
        error.setMessage(errors);
        error.setTimestamp(formatDate());
        error.setUrl(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString());
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorItem> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    public ResponseEntity<ErrorItem> handleInvalidCredentialsException(InvalidCredentialsException e) {
        ErrorItem error = generateMessage(e, HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
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
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<ErrorItem> handleUsernameNotFoundException(UsernameNotFoundException e) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorItem> handleEntityNotFoundException(EntityNotFoundException e) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    /**
     * Generates an ErrorItem object with error message, URL, status code and timestamp.
     *
     * @param e Exception
     * @param status HTTP status
     * @return ErrorItem with populated fields
     */
    public ErrorItem generateMessage(Exception e, HttpStatus status) {
        ErrorItem error = new ErrorItem();
        error.setTimestamp(formatDate());
        error.setMessage(e.getMessage());
        error.setUrl(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString());
        error.setStatusCode(status.value());
        return error;
    }

    /**
     * Formats the current date and time into a string with pattern "yyyy-MM-dd HH:mm".
     *
     * @return formatted date-time string
     */
    public String formatDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTimeFormatter.format(LocalDateTime.now());
    }
}