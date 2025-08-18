# AuthenticationService  
A secure authentication microservice built on Spring Boot 3.3.4 with JWT (RSA) and Feign client integration.

## 🚀 Key Features  
Authentication microservice that provides user login, token issuance (JWT), validation, refreshing, and credential management. Designed to integrate with other services, especially for authorizing access in a UserService.

🚀 Features
- Login via login/password
- Generate JWT tokens (Access + Refresh) using RSA-2048 keys
- Validate tokens
- Refresh tokens
- JWT-based authorization
- Secure integration with User Service via Feign
- Centralized security exception handling
- Dockerized for easy deployment 

## Authentication Flow
- Login via credentials (username/password)
- Generate JWT token pairs:
  - Access Token (short-lived, stored in ThreadLocal)
  - Refresh Token (long-lived, stored in PostgreSQL)
- Token validation and refresh mechanisms 
- Automatic user data propagation to User Service on registration

## ⚙️ Technologies
- Java 21
- Spring Boot 3.3.4
- PostgreSQL 
- MapStruct (DTO mapping)
- Lombok (reducing boilerplate code)
- Feign Client for service-to-service communication
- SpringDoc OpenAPI (API documentation)
- BCrypt for password hashing
- Docker for containerization

## 🔗 Service Integration
- Automatic user data propagation on registration 
- Feign client endpoint: https://github.com/juliakaiko/userservice

## 📩 Contacts
**Author:** Yuliya Kaiko
**Email:** yuliya.kaiko@innowise.com

