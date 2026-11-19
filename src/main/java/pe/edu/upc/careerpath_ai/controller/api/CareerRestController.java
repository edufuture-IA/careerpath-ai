package pe.edu.upc.careerpath_ai.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.CareerDTO;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.service.CareerService;

import java.util.List;

/**
 * Lectura: cualquier usuario autenticado. Escritura (POST/PUT/DELETE): DOCENTE o ADMIN.
 * Las reglas de rol se definen en SecurityConfig.
 */
@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerRestController {

    private final CareerService careerService;

    @GetMapping
    public List<CareerDTO> list() {
        return careerService.findAll().stream().map(CareerDTO::from).toList();
    }

    @GetMapping("/{id}")
    public CareerDTO get(@PathVariable Long id) {
        return careerService.findById(id)
                .map(CareerDTO::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada"));
    }

    @PostMapping
    public ResponseEntity<CareerDTO> create(@Valid @RequestBody CareerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CareerDTO.from(careerService.save(dto.toEntity())));
    }

    @PutMapping("/{id}")
    public CareerDTO update(@PathVariable Long id, @Valid @RequestBody CareerDTO dto) {
        return CareerDTO.from(careerService.update(id, dto.toEntity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        careerService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada"));
        careerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
