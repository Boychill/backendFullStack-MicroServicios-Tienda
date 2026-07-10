package tienda.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.auth.dto.AuthRequest;
import tienda.api.auth.dto.AuthResponse;
import tienda.api.auth.service.AuthService;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private tienda.api.auth.repository.UsuarioRepository usuarioRepository;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en el sistema")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error en los datos de registro o email duplicado")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        Object response = authService.register(request);
        if(response instanceof String && ((String)response).contains("registrado exitosamente")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", response,
                "_links", Map.of("login", linkTo(methodOn(AuthController.class).login(request)).withRel("login"))
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y retorna un token JWT")
    @ApiResponse(responseCode = "200", description = "Login exitoso, retorna token y HATEOAS")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            authResponse.addLink("mis_direcciones", linkTo(methodOn(PerfilController.class).misDirecciones()).withRel("mis_direcciones"));
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/count")
    @Operation(summary = "Contar usuarios", description = "Obtiene la cantidad total de usuarios registrados")
    public ResponseEntity<?> countUsers() {
        return ResponseEntity.ok(Map.of("total", usuarioRepository.count()));
    }

    @GetMapping("/usuarios/choferes")
    @Operation(summary = "Obtener choferes", description = "Lista los IDs de todos los usuarios con rol CHOFER")
    public ResponseEntity<List<Long>> getChoferes() {
        List<Long> choferes = usuarioRepository.findAll().stream()
            .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_CHOFER")))
            .map(tienda.api.auth.model.Usuario::getId)
            .collect(Collectors.toList());
        return ResponseEntity.ok(choferes);
    }
}
