package pe.edu.upc.careerpath_ai.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import pe.edu.upc.careerpath_ai.model.Career;

public record CareerDTO(
        Long id,
        @NotBlank(message = "El nombre es obligatorio") String name,
        String description,
        String area,
        String requiredSkills,
        @DecimalMin(value = "0.0", message = "El índice mínimo es 0")
        @DecimalMax(value = "1.0", message = "El índice máximo es 1") Double demandIndex) {

    public static CareerDTO from(Career c) {
        return new CareerDTO(c.getId(), c.getName(), c.getDescription(), c.getArea(),
                c.getRequiredSkills(), c.getDemandIndex());
    }

    public Career toEntity() {
        return Career.builder()
                .name(name).description(description).area(area)
                .requiredSkills(requiredSkills).demandIndex(demandIndex)
                .build();
    }
}
