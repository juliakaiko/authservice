package com.mymicroservice.authservice;

import com.mymicroservice.authservice.client.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients (clients = UserClient.class)
public class AuthserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthserviceApplication.class, args);
	}

}
