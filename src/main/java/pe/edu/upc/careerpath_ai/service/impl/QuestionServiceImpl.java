package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.Question;
import pe.edu.upc.careerpath_ai.repository.QuestionRepository;
import pe.edu.upc.careerpath_ai.service.QuestionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public List<Question> findAllOrdered() {
        return questionRepository.findAllByOrderByOrderIndexAsc();
    }

    @Override public long count() { return questionRepository.count(); }
    @Override public void save(Question q) { questionRepository.save(q); }

    @Override
    public void delete(Long id) { questionRepository.deleteById(id); }

    @Override
    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta no encontrada"));
    }
}