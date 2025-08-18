package com.mymicroservice.authservice.util;

import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;

import java.time.LocalDate;

public class UserCredentialGenerator {

    public static UserCredential generateUser() {

        return  UserCredential.builder()
                .name("TestName")
                .surname("TestSurName")
                .birthDate(LocalDate.of(2000, 1, 1))
                .email("test@test.by")
                .password("pass_test")
                .role(Role.USER)
                .build();
    }
}
