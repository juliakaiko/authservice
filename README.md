# AuthenticationService  
A microservice for managing users and their cards, built on Spring Boot 3.3.4 using PostgreSQL, Redis, Liquibase, and OpenAPI (Swagger).

## 🚀 Key Features  
Authentication microservice that provides user login, token issuance (JWT), validation, refreshing, and credential management. Designed to integrate with other services, especially for authorizing access in a User Service.

🚀 Features
- Login via login/password
- Generate JWT tokens (Access + Refresh)
- Validate tokens
- Refresh tokens
- Securely store user credentials (hashed using BCrypt with per-password salt)
- JWT-based authorization
- Centralized security exception handling
- Dockerized for easy deployment

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

