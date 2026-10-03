package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Question;
import java.util.List;

public interface QuestionService {
    List<Question> findAllOrdered();
    long count();
    void save(Question q);
    void delete(Long id);          
    Question findById(Long id);    
}