package com.mymicroservice.authservice.client;

import com.mymicroservice.authservice.configuration.UserClientConfig;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "userservice", url = "${user-service.url}",configuration = UserClientConfig.class)
public interface UserClient {

    @PostMapping("/api/users/")  // mapping to endpoint of userservice "createUser()"
    void createUser(@RequestBody UserRegistrationRequest userRegistrationRequest);

}
