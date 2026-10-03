package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "universities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String website;
    private String city;
    private String logoUrl;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String faculties;
}