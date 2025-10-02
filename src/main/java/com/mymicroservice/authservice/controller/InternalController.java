package com.mymicroservice.authservice.controller;

import com.mymicroservice.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/auth")
@Slf4j
public class InternalController {

    private final AuthService authService;
    /**
     * Deletes a user from the AuthService.
     *
     * <p>This endpoint is intended for internal use only by the Gateway service.
     * It requires the header "X-Internal-Call" with value "true" to authorize the request.
     * If the header is missing or incorrect, the request will be rejected with 403 FORBIDDEN.
     *
     * <p>Used in conjunction with the Gateway to perform cascade user deletion across
     * multiple services (e.g., UserService and AuthService) in a controlled, internal-only flow.
     *
     * @param id the ID of the user to delete
     * @param internalCall internal call authorization header, must be "true"
     * @return 204 NO CONTENT if deletion succeeds, 403 FORBIDDEN otherwise
     */
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestHeader(value = "X-Internal-Call", required = false) String internalCall) {
        if (!"true".equals(internalCall)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        authService.deleteUserCredential(id);
        return ResponseEntity.noContent().build();
    }
}
