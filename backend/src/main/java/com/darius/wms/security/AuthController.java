package com.darius.wms.security;

import com.darius.wms.security.TokenService.IssuedToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /** Checks username + password (BCrypt) and returns a JWT. Wrong credentials -> 401. */
    @PostMapping("/login")
    public IssuedToken login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return tokenService.issue(authentication);
    }

    /** Who am I? Resolved from the bearer token. */
    @GetMapping("/me")
    public UserInfo me(Authentication authentication) {
        return new UserInfo(authentication.getName(), CurrentUser.roles(authentication));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record UserInfo(String username, List<String> roles) {
    }
}
