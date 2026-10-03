package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.Favorite;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;

public interface FavoriteService {
    List<Favorite> findByUser(User user);
    Favorite add(User user, Career career);
    void remove(User user, Career career);
    boolean exists(User user, Career career);
}