package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.careerpath_ai.exception.ConflictoException;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.Favorite;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.FavoriteRepository;
import pe.edu.upc.careerpath_ai.service.FavoriteService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Override
    public List<Favorite> findByUser(User user) {
        return favoriteRepository.findByUser(user);
    }

    @Override
    public Favorite add(User user, Career career) {
        if (favoriteRepository.existsByUserAndCareer(user, career)) {
            throw new ConflictoException("La carrera ya está en favoritos");
        }
        return favoriteRepository.save(
                Favorite.builder().user(user).career(career).build());
    }

    @Override
    @Transactional
    public void remove(User user, Career career) {
        favoriteRepository.deleteByUserAndCareer(user, career);
    }

    @Override
    public boolean exists(User user, Career career) {
        return favoriteRepository.existsByUserAndCareer(user, career);
    }
}