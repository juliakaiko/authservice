package com.mymicroservice.authservice.unit.util;

import com.mymicroservice.authservice.advice.GlobalAdvice;
import com.mymicroservice.authservice.util.ErrorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ErrorItemTest {

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void fromMethodArgumentNotValid_ShouldPopulateFieldErrors_WhenValidationFails() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));
        bindingResult.addError(new FieldError("target", "password", "size must be between 5 and 255"));
        MethodParameter methodParameter = new MethodParameter(
                GlobalAdvice.class.getDeclaredMethod("handleMethodArgumentNotValidException", MethodArgumentNotValidException.class),
                0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ErrorItem error = ErrorItem.fromMethodArgumentNotValid(exception, HttpStatus.BAD_REQUEST);

        assertEquals("Validation failed", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertEquals("must not be blank", error.getFieldErrors().get("email"));
        assertEquals("size must be between 5 and 255", error.getFieldErrors().get("password"));
        assertNotNull(error.getTimestamp());
        assertTrue(error.getUrl().endsWith("/api/test"));
    }

    @Test
    void generateMessage_ShouldPopulateErrorFields_WhenExceptionProvided() {
        Exception exception = new IllegalArgumentException("Invalid request");

        ErrorItem error = ErrorItem.generateMessage(exception, HttpStatus.BAD_REQUEST);

        assertEquals("Invalid request", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertTrue(error.getUrl().endsWith("/api/test"));
        assertNotNull(error.getTimestamp());
    }

    @Test
    void formatDate_ShouldReturnFormattedTimestamp_WhenCalled() {
        String formattedDate = ErrorItem.formatDate();

        assertNotNull(formattedDate);
        assertTrue(formattedDate.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }
}
