package tienda.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @Email(message = "Email no válido")
    @NotBlank(message = "El email es requerido")
    private String email;

    @NotBlank(message = "El password es requerido")
    private String password;

    private String role;
}
