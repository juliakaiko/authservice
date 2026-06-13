package com.mymicroservice.authservice.util;

import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;

import static com.mymicroservice.authservice.util.data.TestConstants.USER_BIRTH_DATE;
import static com.mymicroservice.authservice.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.authservice.util.data.TestConstants.USER_ID;
import static com.mymicroservice.authservice.util.data.TestConstants.USER_NAME;
import static com.mymicroservice.authservice.util.data.TestConstants.USER_PASSWORD;
import static com.mymicroservice.authservice.util.data.TestConstants.USER_SURNAME;

public class UserCredentialGenerator {

    public static UserCredential generateUser() {
        return UserCredential.builder()
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .birthDate(USER_BIRTH_DATE)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .role(Role.USER)
                .build();
    }

    public static UserCredential generateUserWithId() {
        UserCredential user = generateUser();
        user.setUserId(USER_ID);
        return user;
    }
}
