package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.dto.ContactForm;
import pe.edu.upc.careerpath_ai.exception.ConflictoException;
import pe.edu.upc.careerpath_ai.model.ContactMessage;
import pe.edu.upc.careerpath_ai.model.NewsletterSubscriber;
import pe.edu.upc.careerpath_ai.repository.ContactMessageRepository;
import pe.edu.upc.careerpath_ai.repository.NewsletterSubscriberRepository;
import pe.edu.upc.careerpath_ai.service.ContactService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactRepo;
    private final NewsletterSubscriberRepository newsletterRepo;

    @Override
    public ContactMessage submitContact(ContactForm form) {
        return contactRepo.save(ContactMessage.builder()
                .fullName(form.getFullName())
                .email(form.getEmail())
                .institutionName(form.getInstitutionName())
                .phone(form.getPhone())
                .message(form.getMessage())
                .createdAt(LocalDateTime.now())
                .attended(false)
                .build());
    }

    @Override
    public List<ContactMessage> allMessages() {
        return contactRepo.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public NewsletterSubscriber subscribe(String email) {
        if (newsletterRepo.existsByEmail(email)) {
            throw new ConflictoException("Este correo ya está suscrito");
        }
        return newsletterRepo.save(NewsletterSubscriber.builder()
                .email(email)
                .subscribedAt(LocalDateTime.now())
                .active(true)
                .build());
    }
}