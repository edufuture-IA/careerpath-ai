package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByStudentOrderByCompletedAtDesc(User student);
    List<TestResult> findAllByOrderByCompletedAtDesc();

    // 🆕
    Optional<TestResult> findByShareToken(String shareToken);
}