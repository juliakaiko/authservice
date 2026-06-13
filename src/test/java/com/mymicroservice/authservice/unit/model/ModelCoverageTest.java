package com.mymicroservice.authservice.unit.model;

import com.mymicroservice.authservice.model.RefreshToken;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.util.RefreshTokenGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ModelCoverageTest {

    @Test
    void refreshToken_ShouldSupportEqualsAndHashCode_WhenSameValues() {
        RefreshToken first = RefreshTokenGenerator.generateRefreshToken();
        RefreshToken second = RefreshTokenGenerator.generateRefreshToken();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void role_ShouldExposeAuthority_WhenEnumValuesUsed() {
        assertEquals("USER", Role.USER.getAuthority());
        assertEquals("ADMIN", Role.ADMIN.getAuthority());
        assertNotEquals(Role.USER, Role.ADMIN);
    }
}
