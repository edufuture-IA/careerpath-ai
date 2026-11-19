package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "careers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String area;

    @Column(length = 500)
    private String requiredSkills;

    @Column(nullable = false)
    @Builder.Default
    private Double demandIndex = 0.0;

    private LocalDateTime demandIndexUpdatedAt;

    @Column(length = 20)
    private String demandIndexSource;

    // 🆕 NUEVOS CAMPOS PARA IA
    @Column(length = 2500)
    private String aiDayInTheLife;

    @Column(length = 1000)
    private String aiSimpleExplanation;
}