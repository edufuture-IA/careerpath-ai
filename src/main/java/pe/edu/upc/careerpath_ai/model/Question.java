package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    // Área evaluada: "Ingeniería", "Salud", "Arte", "Negocios", "Legal"
    @Column(nullable = false, length = 50)
    private String area;

    // Orden de la pregunta en el test
    private Integer orderIndex;
}