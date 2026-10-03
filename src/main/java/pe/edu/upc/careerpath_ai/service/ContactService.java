package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.dto.ContactForm;
import pe.edu.upc.careerpath_ai.model.ContactMessage;
import pe.edu.upc.careerpath_ai.model.NewsletterSubscriber;
import java.util.List;

public interface ContactService {
    ContactMessage submitContact(ContactForm form);
    List<ContactMessage> allMessages();
    NewsletterSubscriber subscribe(String email);
}