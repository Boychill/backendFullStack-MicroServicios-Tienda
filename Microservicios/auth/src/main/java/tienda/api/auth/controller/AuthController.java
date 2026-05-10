package tienda.api.auth.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.auth.dto.AuthRequest;
import tienda.api.auth.service.AuthService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        Object response = authService.register(request);
        if(response instanceof String && ((String)response).contains("registrado exitosamente")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", response));
        }
        return ResponseEntity.badRequest().body(Map.of("error", response));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
}
