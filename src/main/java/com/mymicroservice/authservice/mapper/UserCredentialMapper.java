package com.mymicroservice.authservice.mapper;

import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.model.UserCredential;
import lombok.NonNull;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserCredentialMapper {

    UserCredentialMapper INSTANSE = Mappers.getMapper (UserCredentialMapper.class);

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "surname", source = "user.surname")
    @Mapping(target = "birthDate", source = "user.birthDate")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "password", source = "user.password")
    @Mapping(target = "role", source = "user.role")
    UserRegistrationRequest toDto(UserCredential user);

    /**
     * Converts {@link UserRegistrationRequest} back to {@link UserCredential} entity.
     * <p>
     * Implements <b>reverse mapping</b> relative to {@code User -> UserDto} conversion.
     * </p>
     *
     * @param userDto DTO object to convert (cannot be {@code null})
     * @return corresponding {@link UserCredential} entity
     */
    @InheritInverseConfiguration
    UserCredential toEntity (@NonNull UserRegistrationRequest userDto);
}
