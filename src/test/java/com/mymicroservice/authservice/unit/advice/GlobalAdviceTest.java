package com.mymicroservice.authservice.unit.advice;

import com.mymicroservice.authservice.advice.GlobalAdvice;
import com.mymicroservice.authservice.exception.InvalidCredentialsException;
import com.mymicroservice.authservice.exception.UserCredentialNotFoundException;
import com.mymicroservice.authservice.util.ErrorItem;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GlobalAdviceTest {

    private GlobalAdvice globalAdvice;

    @BeforeEach
    void setUp() {
        globalAdvice = new GlobalAdvice();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));
        MethodParameter methodParameter = new MethodParameter(
                GlobalAdvice.class.getDeclaredMethod("handleMethodArgumentNotValidException", MethodArgumentNotValidException.class),
                0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorItem> response = globalAdvice.handleMethodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("must not be blank", response.getBody().getFieldErrors().get("email"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest_WhenArgumentIsInvalid() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorItem> response = globalAdvice.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleInvalidCredentialsException_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Incorrect email or password");

        ResponseEntity<ErrorItem> response = globalAdvice.handleInvalidCredentialsException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Incorrect email or password", response.getBody().getMessage());
    }

    @Test
    void handleBadCredentialsException_ShouldReturnBadRequest_WhenDataIntegrityViolated() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Duplicate email");

        ResponseEntity<ErrorItem> response = globalAdvice.handleBadCredentialsException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Duplicate email", response.getBody().getMessage());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturnBadRequest_WhenBodyIsMalformed() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<ErrorItem> response = globalAdvice.handleHttpMessageNotReadableException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Malformed JSON", response.getBody().getMessage());
    }

    @Test
    void handleUsernameNotFoundException_ShouldReturnBadRequest_WhenUserNotFound() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        ResponseEntity<ErrorItem> response = globalAdvice.handleUsernameNotFoundException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleUserCredentialNotFoundException_ShouldReturnBadRequest_WhenCredentialNotFound() {
        UserCredentialNotFoundException exception = new UserCredentialNotFoundException("Credential not found");

        ResponseEntity<ErrorItem> response = globalAdvice.handleUserCredentialNotFoundException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Credential not found", response.getBody().getMessage());
    }

    @Test
    void handleEntityNotFoundException_ShouldReturnBadRequest_WhenEntityNotFound() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        ResponseEntity<ErrorItem> response = globalAdvice.handleEntityNotFoundException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Entity not found", response.getBody().getMessage());
    }
}
