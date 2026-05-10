package tienda.api.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.auth.model.Direccion;
import tienda.api.auth.repository.DireccionRepository;
import java.util.List;

@Service
public class PerfilService {

    @Autowired private DireccionRepository direccionRepository;

    public List<Direccion> listarMisDirecciones(String email) {
        return direccionRepository.findByUsuarioEmail(email);
    }

    public Direccion agregarDireccion(String email, Direccion nueva) {
        List<Direccion> actuales = direccionRepository.findByUsuarioEmail(email);
        if (actuales.isEmpty()) {
            nueva.setEsPrincipal(true);
        } else if (Boolean.TRUE.equals(nueva.getEsPrincipal())) {
            for(Direccion d : actuales) {
                d.setEsPrincipal(false);
                direccionRepository.save(d);
            }
        }
        nueva.setUsuarioEmail(email);
        return direccionRepository.save(nueva);
    }

    public void eliminarDireccion(String email, Long id) {
        Direccion dir = direccionRepository.findById(id).orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        if(!dir.getUsuarioEmail().equals(email)) {
            throw new RuntimeException("Acceso denegado a esta libreta de dirección");
        }
        direccionRepository.delete(dir);
    }
}
