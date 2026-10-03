package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms",
       uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String grade;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    // 🆕 Alumnos del aula (EAGER para simplificar)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "classroom_students",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    @Builder.Default
    private List<User> students = new ArrayList<>();

    // 🆕 Docentes del aula (NUEVO)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "classroom_teachers",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id"))
    @Builder.Default
    private List<User> teachers = new ArrayList<>();
}