package com.mymicroservice.authservice.util.data;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;

@UtilityClass
public class TestConstants {

    public static final String USER_NAME = "TestName";
    public static final String USER_SURNAME = "TestSurName";
    public static final LocalDate USER_BIRTH_DATE = LocalDate.of(2000, 2, 2);
    public static final String USER_EMAIL = "test@test.by";
    public static final String USER_PASSWORD = "pass_test";

    public static final String LOGIN_EMAIL = "testuser@test.by";
    public static final String LOGIN_PASSWORD = "testpass";

    public static final Long USER_ID = 1L;
    public static final Long SECOND_USER_ID = 2L;

    public static final String ACCESS_TOKEN = "access-token";
    public static final String REFRESH_TOKEN = "refresh-token";
    public static final String NEW_ACCESS_TOKEN = "new-access-token";
    public static final String NEW_REFRESH_TOKEN = "new-refresh-token";
    public static final String OLD_REFRESH_TOKEN = "old-refresh-token";
    public static final String VALID_TOKEN = "valid-token";
    public static final String INVALID_TOKEN = "invalid-token";
    public static final String INVALID_JWT_STRING = "invalid.token.string";

    public static final String ENCODED_PASSWORD = "encodedPassword";
    public static final String MOCK_ACCESS_TOKEN = "accessToken";
    public static final String MOCK_REFRESH_TOKEN = "refreshToken";

    public static final String NON_EXISTING_EMAIL = "non-existing-email";
    public static final String INTERNAL_CALL_HEADER = "X-Internal-Call";
    public static final String INTERNAL_CALL_TRUE = "true";
    public static final String INTERNAL_CALL_FALSE = "false";

    public static final String REFRESH_TOKEN_VALUE = "newRefreshToken";
    public static final LocalDateTime REFRESH_TOKEN_EXPIRES_AT = LocalDateTime.of(2025, 8, 4, 22, 17, 37);
    public static final LocalDateTime REFRESH_TOKEN_ISSUED_AT = LocalDateTime.of(2025, 8, 5, 22, 17, 37);

    public static final String POSTGRES_IMAGE = "postgres:15-alpine";
    public static final String TEST_DB_NAME = "testdb";
    public static final String TEST_DB_USER = "user";
    public static final String TEST_DB_PASSWORD = "password";

    public static final String ROLE_USER = "USER";
    public static final int RSA_KEY_SIZE = 2048;
}
