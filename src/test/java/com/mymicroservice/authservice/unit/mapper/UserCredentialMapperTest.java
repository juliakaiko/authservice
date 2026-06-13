package com.mymicroservice.authservice.unit.mapper;

import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.mapper.UserCredentialMapper;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import com.mymicroservice.authservice.util.data.TestConstants;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserCredentialMapperTest {

    private final UserCredentialMapper mapper = Mappers.getMapper(UserCredentialMapper.class);

    @Test
    void toDto_ShouldMapAllFields_WhenEntityIsProvided() {
        UserCredential entity = UserCredentialGenerator.generateUserWithId();

        UserRegistrationRequest dto = mapper.toDto(entity);

        assertEquals(TestConstants.USER_NAME, dto.getName());
        assertEquals(TestConstants.USER_SURNAME, dto.getSurname());
        assertEquals(TestConstants.USER_BIRTH_DATE, dto.getBirthDate());
        assertEquals(TestConstants.USER_EMAIL, dto.getEmail());
        assertEquals(TestConstants.USER_PASSWORD, dto.getPassword());
        assertEquals(Role.USER, dto.getRole());
    }

    @Test
    void toEntity_ShouldMapAllFields_WhenDtoIsProvided() {
        UserRegistrationRequest dto = mapper.toDto(UserCredentialGenerator.generateUser());

        UserCredential entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(TestConstants.USER_NAME, entity.getName());
        assertEquals(TestConstants.USER_EMAIL, entity.getEmail());
        assertEquals(Role.USER, entity.getRole());
    }
}
