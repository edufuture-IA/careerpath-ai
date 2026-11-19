package pe.edu.upc.careerpath_ai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String institutionName;
    private String phone;

    @Column(length = 2000, nullable = false)
    private String message;

    private LocalDateTime createdAt;

    @Builder.Default
    private boolean attended = false;
}