package com.mymicroservice.authservice.unit.model;

import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import com.mymicroservice.authservice.util.data.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserCredentialTest {

    @Test
    void getAuthorities_ShouldReturnRoleAuthority_WhenUserHasRole() {
        var user = UserCredentialGenerator.generateUser();

        var authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_" + Role.USER.name())));
    }

    @Test
    void getUsername_ShouldReturnEmail_WhenUserIsCreated() {
        var user = UserCredentialGenerator.generateUser();

        assertEquals(TestConstants.USER_EMAIL, user.getUsername());
    }

    @Test
    void userDetailsFlags_ShouldBeTrue_WhenUserIsActive() {
        var user = UserCredentialGenerator.generateUser();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}
