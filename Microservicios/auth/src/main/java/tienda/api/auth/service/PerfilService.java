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

    public Direccion actualizarDireccion(String email, Long id, Direccion nuevosDatos) {
        Direccion existente = direccionRepository.findById(id).orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        if(!existente.getUsuarioEmail().equals(email)) {
            throw new RuntimeException("Acceso denegado a esta libreta de dirección");
        }

        existente.setAlias(nuevosDatos.getAlias());
        existente.setDireccionEscrita(nuevosDatos.getDireccionEscrita());
        existente.setLatitud(nuevosDatos.getLatitud());
        existente.setLongitud(nuevosDatos.getLongitud());

        if (Boolean.TRUE.equals(nuevosDatos.getEsPrincipal()) && !Boolean.TRUE.equals(existente.getEsPrincipal())) {
            List<Direccion> actuales = direccionRepository.findByUsuarioEmail(email);
            for(Direccion d : actuales) {
                if(!d.getId().equals(existente.getId()) && Boolean.TRUE.equals(d.getEsPrincipal())) {
                    d.setEsPrincipal(false);
                    direccionRepository.save(d);
                }
            }
            existente.setEsPrincipal(true);
        } else if (Boolean.FALSE.equals(nuevosDatos.getEsPrincipal())) {
            existente.setEsPrincipal(false);
        }

        return direccionRepository.save(existente);
    }
}
