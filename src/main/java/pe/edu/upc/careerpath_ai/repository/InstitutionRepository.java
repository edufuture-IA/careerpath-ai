package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.Institution;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByInvitationCode(String invitationCode);
    boolean existsByName(String name);
    boolean existsByContactEmail(String contactEmail);
}