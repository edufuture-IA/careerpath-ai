package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private LocalDateTime completedAt;

    private String topArea;

    @Column(length = 1000)
    private String areaScores;

    @Column(length = 1000)
    private String recommendedCareers;

    @Column(unique = true)
    private String shareToken;

    @Column(length = 2000)
    private String aiAnalysis;

    // 🆕 NUEVO CAMPO PARA EL ROADMAP
    @Column(length = 2500)
    private String aiRoadmap;

    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
}