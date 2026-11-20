package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

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

    // US21: carreras que ofrece la universidad (base de las estadisticas de afinidad)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "university_careers",
            joinColumns = @JoinColumn(name = "university_id"),
            inverseJoinColumns = @JoinColumn(name = "career_id"))
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Career> careers = new LinkedHashSet<>();
}