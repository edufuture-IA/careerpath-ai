package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String action;  // CREATE_USER, DELETE_CAREER, LOGIN, etc.

    @Column(length = 500)
    private String description;

    @Column(length = 150)
    private String username;

    @Column(length = 50)
    private String role;

    private LocalDateTime timestamp;

    @Column(length = 100)
    private String ipAddress;
}