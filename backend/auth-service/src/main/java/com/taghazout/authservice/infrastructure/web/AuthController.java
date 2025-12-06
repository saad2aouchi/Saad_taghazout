package com.taghazout.authservice.infrastructure.web;

import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.LoginRequest;
import com.taghazout.authservice.application.dto.RegisterRequest;
import com.taghazout.authservice.application.usecase.AuthenticateUserUseCase;
import com.taghazout.authservice.application.usecase.CreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for HTTP request/response handling
 * - DIP: Depends on use case abstractions (not implementations)
 * - OCP: Can be extended with additional endpoints
 * 
 * Constructor Injection:
 * - No @Autowired on fields (per requirement)
 * - Immutable dependencies
 * - Easy to test with mocks
 * 
 * OpenAPI Documentation:
 * - Swagger annotations for API docs
 * - Available at /auth/openapi.json
 * - Swagger UI at /swagger-ui.html
 * 
 * Endpoints:
 * - POST /api/v1/auth/register - Create new user (201 Created)
 * - POST /api/v1/auth/login - Authenticate user (200 OK)
 * 
 * Validation:
 * - @Valid triggers Jakarta validation on DTOs
 * - Returns 400 Bad Request for invalid input
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final CreateUserUseCase createUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    /**
     * Constructor injection (no field injection).
     * 
     * @param createUserUseCase       user registration use case
     * @param authenticateUserUseCase user authentication use case
     */
    public AuthController(
            CreateUserUseCase createUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    /**
     * Register a new user.
     * 
     * Creates user account, hashes password, and returns JWT tokens.
     * 
     * @param request registration request (email, password, optional name)
     * @return 201 Created with AuthResponse containing tokens
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with email and password. Returns JWT access and refresh tokens.", responses = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid input (validation error)")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = createUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user (login).
     * 
     * Validates credentials and returns JWT tokens.
     * 
     * @param request login request (email, password)
     * @return 200 OK with AuthResponse containing tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user with email and password. Returns JWT access and refresh tokens.", responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account disabled/locked"),
            @ApiResponse(responseCode = "400", description = "Invalid input (validation error)")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticateUserUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
