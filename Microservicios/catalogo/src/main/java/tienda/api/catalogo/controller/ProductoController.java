package tienda.api.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.api.catalogo.dto.ProductoRequestDto;
import tienda.api.catalogo.dto.ProductoResponseDto;
import tienda.api.catalogo.model.Producto;
import tienda.api.catalogo.service.ProductoService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Catálogo", description = "Endpoints para la gestión y visualización de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos", description = "Obtiene una lista de productos, opcionalmente filtrada por categoría.")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
    public ResponseEntity<CollectionModel<ProductoResponseDto>> listar(@RequestParam(required = false) String categoria) {
        List<Producto> productos = productoService.obtenerTodos(categoria);
        
        List<ProductoResponseDto> dtos = productos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        CollectionModel<ProductoResponseDto> collectionModel = CollectionModel.of(dtos);
        collectionModel.add(linkTo(methodOn(ProductoController.class).listar(categoria)).withSelfRel());
        
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Devuelve el detalle de un producto específico.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        return productoService.obenterPorId(id)
                .map(producto -> ResponseEntity.ok((Object)convertToDto(producto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No encontrado")));
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el catálogo. Requiere permisos.")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoRequestDto productoDto) {
        Producto nuevo = productoService.guardar(productoDto);
        ProductoResponseDto dto = convertToDto(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Producto creado", "producto", dto));
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Actualizar stock", description = "Sincroniza el stock de exhibición del producto.")
    public ResponseEntity<?> actualizarStockSync(@PathVariable Long id, @RequestParam Integer stock) {
        productoService.actualizarStock(id, stock);
        return ResponseEntity.ok(Map.of("mensaje", "Stock de exhibición sincronizado síncronamente en el Catálogo a " + stock,
                "_links", Map.of("producto", linkTo(methodOn(ProductoController.class).detalle(id)).withRel("producto"))));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Activa o desactiva un producto del catálogo.")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        productoService.cambiarEstadoProducto(id, activo);
        return ResponseEntity.ok(Map.of("mensaje", "Estado del producto actualizado a: " + (activo ? "Activo" : "Inactivo"),
                "_links", Map.of("producto", linkTo(methodOn(ProductoController.class).detalle(id)).withRel("producto"))));
    }

    private ProductoResponseDto convertToDto(Producto producto) {
        ProductoResponseDto dto = ProductoResponseDto.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .categoria(producto.getCategoria())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .build();
        
        dto.add(linkTo(methodOn(ProductoController.class).detalle(producto.getId())).withSelfRel());
        dto.add(linkTo(methodOn(ProductoController.class).cambiarEstado(producto.getId(), !producto.getActivo())).withRel("cambiarEstado"));
        
        return dto;
    }
}
