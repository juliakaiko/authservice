# AuthenticationService  
A secure authentication microservice built on Spring Boot 3.3.4 with JWT (RSA).

## 🚀 Key Features  
Authentication microservice that provides user login, token issuance (JWT), validation, refreshing, and credential management. 

🚀 Features
- Login via login/password
- Generate JWT tokens (Access + Refresh) using JWS with RS256 — a digital signature scheme combining:
  - RSA (asymmetric cryptography) for signing with a private key and verification with a public key. 
  - SHA-256 (hashing) for integrity checks.
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
- SpringDoc OpenAPI (API documentation)
- BCrypt for password hashing
- Docker for containerization

## 📩 Contacts
**Author:** Yuliya Kaiko
**Email:** yuliya.kaiko@innowise.com

