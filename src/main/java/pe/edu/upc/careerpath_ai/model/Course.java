package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String provider;
    private String url;

    @Column(length = 100)
    private String area;

    @Column(length = 100)
    private String skill;

    private Integer durationHours;
}