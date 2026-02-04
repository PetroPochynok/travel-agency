# Travel Agency Application

## Overview

This is a Spring Boot travel agency application with a front-end built using Thymeleaf. It supports user management, voucher management, and secure operations using JWT authentication. The application includes localization, validation, exception handling, logging, and unit testing.

---

## Features

* User management: registration, update, activation/deactivation, deposit/withdraw.
* Voucher management: create, order, cancel, reregister, filter, search.
* Role-based security: ADMIN, MANAGER, CUSTOMER.
* Stateless authentication using JWT.
* Password hashing with BCrypt.
* Front-end using Thymeleaf with multi-language support (EN, UA, FR, DE, ES).
* Logging for service methods and controllers with AOP.
* Exception handling with meaningful, localized messages.
* Unit testing for services.
* HTTPS support.

---

## Technologies Used

* Spring Boot
* Spring Data JPA
* Spring Security
* Thymeleaf
* JWT authentication
* MySQL
* Lombok
* AOP (Logging)
* JUnit + Mockito (Unit tests)

---

## Database Setup

Run the provided SQL script to create the database and initial data:

```bash
sql-scripts/travel-agency.sql
```

This script will:

1. Create `travel_agency` database.
2. Create `users` table with ADMIN, MANAGER, CUSTOMER roles.
3. Create `vouchers` table with relevant fields.
4. Insert sample users with encoded passwords (`12345`) and sample vouchers.

**Important**: Make sure your MySQL server is running.

---

## Application Configuration

Update `application.properties`:

```properties
# HTTPS
server.port=8443
server.ssl.key-store=classpath:travel_keystore.p12
server.ssl.key-store-password=MyPassword123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=travelagency

# DATABASE
spring.datasource.url=jdbc:mysql://localhost:3306/travel_agency
spring.datasource.username=root
spring.datasource.password=pochynok
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / HIBERNATE
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# SECURITY
spring.security.enabled=true
logging.level.org.springframework.security=DEBUG

# JWT
application.security.jwt.secret-key=404E635263336A586E3272357538782F413F4428472B4B6250645367566B5970
application.security.jwt.expiration=86400000
application.security.jwt.refresh-token.expiration=172800000

# Localization
spring.messages.basename=messages
spring.messages.encoding=UTF-8
```

---

## Security

* Passwords are hashed using `BCryptPasswordEncoder`.
* JWT is used for stateless authentication.
* Role-based access control is implemented using `@EnableMethodSecurity` and Spring Security.
* Active users only: users with `active=false` cannot log in.
* Method-level and URL-level access control are applied in `SecurityConfig`.

---

## Logging & AOP

* All service methods and controllers are logged using an `@Aspect` (`LoggingAspect`).
* Logs include method entry, exit, execution time, and exceptions.
* Sensitive fields (like passwords) are masked in logs using the `@Sensitive` annotation.

---

## Localization

* Supported languages: English (default), Ukrainian, French, German, Spanish.
* All front-end labels and validation messages use messages from `messages_*.properties`.
* Example property:

```properties
balance.withdraw.success=Withdraw successful
myVouchers.cancelReasonPlaceholder=Reason
```

---

## API Endpoints

* `/api/users/withdraw` – withdraw funds.
* `/api/auth/**` – authentication endpoints.
* `/login` – login page.
* `/register` – user registration.

---

## Front-End

* Thymeleaf templates with Bootstrap for styling.
* Localized messages injected using `th:text="#\{property.key\}"`.
* Navigation buttons redirect to HTTPS endpoints (e.g., `/catalog`).
* Forms include client-side validation.

---

## Unit Testing

* Services are tested using JUnit 5 and Mockito.
* Example: `UserServiceImplTest` for `register()` method, including:

    * Username already exists
    * Password encoding
    * Successful registration

---

## Running the Application

1. Run MySQL and execute `sql-scripts/travel-agency.sql`.
2. Update `application.properties` with your database credentials.
3. Build and run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

4. Access the application via:

```
https://localhost:8443
```

---

## Notes

* All security and exception handling requirements are met.
* Validation and localized error messages are included.
* Logging tracks business logic and security events.
* Optional features implemented: searching, pagination, sorting, JWT-based authentication.
