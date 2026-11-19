package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.Favorite;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndCareer(User user, Career career);
    boolean existsByUserAndCareer(User user, Career career);
    void deleteByUserAndCareer(User user, Career career);
}