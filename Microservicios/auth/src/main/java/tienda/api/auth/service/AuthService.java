package tienda.api.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tienda.api.auth.config.JwtProvider;
import tienda.api.auth.dto.AuthRequest;
import tienda.api.auth.dto.AuthResponse;
import tienda.api.auth.dto.RoleDto;
import tienda.api.auth.dto.UsuarioDto;
import tienda.api.auth.model.Role;
import tienda.api.auth.model.Usuario;
import tienda.api.auth.repository.RoleRepository;
import tienda.api.auth.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    public Object register(AuthRequest request) {
        if(usuarioRepository.existsByEmail(request.getEmail())) {
            return "Email ya registrado";
        }

        Role userRole = roleRepository.findByNombre("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().nombre("ROLE_USER").build()));

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(userRole))
                .build();
        usuarioRepository.save(usuario);
        return "Usuario registrado exitosamente";
    }

    public AuthResponse login(AuthRequest request) {
        Optional<Usuario> optionalUser = usuarioRepository.findByEmail(request.getEmail());
        if(optionalUser.isPresent()) {
            Usuario usuario = optionalUser.get();
            if(passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
                List<String> rolesStr = usuario.getRoles().stream()
                        .map(Role::getNombre)
                        .collect(Collectors.toList());

                String token = jwtProvider.createToken(usuario.getEmail(), rolesStr);

                List<RoleDto> rolesDto = usuario.getRoles().stream()
                        .map(r -> new RoleDto(r.getNombre()))
                        .collect(Collectors.toList());
                UsuarioDto usuarioDto = new UsuarioDto(usuario.getEmail(), rolesDto);

                return new AuthResponse(token, "Bearer", usuarioDto);
            }
        }
        throw new RuntimeException("Credenciales inválidas");
    }
}
