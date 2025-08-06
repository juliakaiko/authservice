package com.mymicroservice.authservice.security;

import org.springframework.stereotype.Component;

@Component
public class AccessTokenProvider {
    //  AccessToken is stored in ThreadLocal
    private static final ThreadLocal<String> tokenStorage = new ThreadLocal<>();

    public void setAccessToken(String token) {
        tokenStorage.set(token);
    }

    public String getAccessToken() {
        return tokenStorage.get();
    }

    public void clear() {
        tokenStorage.remove();
    }
}
