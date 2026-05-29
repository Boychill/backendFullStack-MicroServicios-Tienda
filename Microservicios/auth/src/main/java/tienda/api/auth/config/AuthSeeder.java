package tienda.api.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tienda.api.auth.model.Role;
import tienda.api.auth.model.Usuario;
import tienda.api.auth.repository.RoleRepository;
import tienda.api.auth.repository.UsuarioRepository;

import java.util.List;

@Component
public class AuthSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            String encryptedPassword = passwordEncoder.encode("123456");
            
            Role roleAdmin = roleRepository.findByNombre("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().nombre("ROLE_ADMIN").build()));
            Role roleBodeguero = roleRepository.findByNombre("ROLE_BODEGUERO")
                    .orElseGet(() -> roleRepository.save(Role.builder().nombre("ROLE_BODEGUERO").build()));
            Role roleUser = roleRepository.findByNombre("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().nombre("ROLE_USER").build()));

            Role roleChofer = roleRepository.findByNombre("ROLE_CHOFER")
                    .orElseGet(() -> roleRepository.save(Role.builder().nombre("ROLE_CHOFER").build()));

            Role roleLogistica = roleRepository.findByNombre("ROLE_LOGISTICA")
                    .orElseGet(() -> roleRepository.save(Role.builder().nombre("ROLE_LOGISTICA").build()));

            List<Usuario> usuariosBase = List.of(
                    Usuario.builder()
                            .email("admin@tienda.com")
                            .password(encryptedPassword)
                            .roles(List.of(roleAdmin))
                            .build(),
                    Usuario.builder()
                            .email("bodega@tienda.com")
                            .password(encryptedPassword)
                            .roles(List.of(roleBodeguero))
                            .build(),
                    Usuario.builder()
                            .email("cliente@tienda.com")
                            .password(encryptedPassword)
                            .roles(List.of(roleUser))
                            .build(),
                    Usuario.builder()
                            .email("chofer@tienda.com")
                            .password(encryptedPassword)
                            .roles(List.of(roleChofer))
                            .build(),
                    Usuario.builder()
                            .email("logistica@tienda.com")
                            .password(encryptedPassword)
                            .roles(List.of(roleLogistica))
                            .build()
            );

            usuarioRepository.saveAll(usuariosBase);
            System.out.println("====== [SEEDER] Usuarios base (Admin, Bodeguero, Cliente, Chofer, Logistica) inicializados con éxito. ======");
        }
    }
}
