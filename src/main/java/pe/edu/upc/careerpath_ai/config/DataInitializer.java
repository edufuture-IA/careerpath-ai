package pe.edu.upc.careerpath_ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upc.careerpath_ai.model.*;
import pe.edu.upc.careerpath_ai.repository.*;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CareerRepository careerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final UniversityRepository universityRepository;
    private final ActivityLogRepository activityLogRepository;   // 🆕
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        seedCareers();
        seedUsers();
        seedQuestions();
        seedCourses();
        seedUniversities();
        seedActivityLogs();   // 🆕
    }

    // =====================================================
    // CARRERAS
    // =====================================================
    private void seedCareers() {
        if (careerRepository.count() == 0) {
            careerRepository.save(Career.builder()
                    .name("Ingeniería de Sistemas")
                    .description("Diseño y desarrollo de software")
                    .area("Ingeniería")
                    .requiredSkills("lógica,programación,matemática")
                    .demandIndex(0.95).build());

            careerRepository.save(Career.builder()
                    .name("Ingeniería Civil")
                    .description("Diseño y construcción de infraestructura")
                    .area("Ingeniería")
                    .requiredSkills("matemática,física,gestión")
                    .demandIndex(0.88).build());

            careerRepository.save(Career.builder()
                    .name("Medicina Humana")
                    .description("Diagnóstico y tratamiento de enfermedades")
                    .area("Salud")
                    .requiredSkills("biología,empatía,química")
                    .demandIndex(0.85).build());

            careerRepository.save(Career.builder()
                    .name("Psicología")
                    .description("Estudio del comportamiento humano")
                    .area("Salud")
                    .requiredSkills("empatía,escucha,análisis")
                    .demandIndex(0.75).build());

            careerRepository.save(Career.builder()
                    .name("Administración de Empresas")
                    .description("Gestión de organizaciones")
                    .area("Negocios")
                    .requiredSkills("liderazgo,comunicación,análisis")
                    .demandIndex(0.80).build());

            careerRepository.save(Career.builder()
                    .name("Marketing Digital")
                    .description("Estrategias de promoción digital")
                    .area("Negocios")
                    .requiredSkills("creatividad,analítica,comunicación")
                    .demandIndex(0.82).build());

            careerRepository.save(Career.builder()
                    .name("Diseño Gráfico")
                    .description("Comunicación visual y creativa")
                    .area("Arte")
                    .requiredSkills("creatividad,dibujo,software")
                    .demandIndex(0.70).build());

            careerRepository.save(Career.builder()
                    .name("Derecho")
                    .description("Defensa y asesoría legal")
                    .area("Legal")
                    .requiredSkills("argumentación,lectura,ética")
                    .demandIndex(0.78).build());

            System.out.println("✅ 8 carreras creadas");
        }
    }

    // =====================================================
    // USUARIOS (verificación individual)
    // =====================================================
    private void seedUsers() {
        // Admin — se crea si no existe
        if (userRepository.findByUsername("admin@upc.edu.pe").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin@upc.edu.pe")
                    .password(encoder.encode("admin123"))
                    .fullName("Admin Demo")
                    .role(Role.ADMIN)
                    .build());
            System.out.println("✅ ADMIN creado: admin@upc.edu.pe / admin123");
        }

        // Docente — se crea si no existe
        if (userRepository.findByUsername("docente@upc.edu.pe").isEmpty()) {
            userRepository.save(User.builder()
                    .username("docente@upc.edu.pe")
                    .password(encoder.encode("docente123"))
                    .fullName("Docente Demo")
                    .role(Role.DOCENTE)
                    .build());
            System.out.println("✅ DOCENTE creado: docente@upc.edu.pe / docente123");
        }

        // Estudiante — se crea si no existe
        if (userRepository.findByUsername("alumno@upc.edu.pe").isEmpty()) {
            userRepository.save(User.builder()
                    .username("alumno@upc.edu.pe")
                    .password(encoder.encode("alumno123"))
                    .fullName("Alumno Demo")
                    .role(Role.ESTUDIANTE)
                    .build());
            System.out.println("✅ ESTUDIANTE creado: alumno@upc.edu.pe / alumno123");
        }
    }

    // =====================================================
    // PREGUNTAS DEL TEST
    // =====================================================
    private void seedQuestions() {
        if (questionRepository.count() == 0) {
            String[][] data = {
                    {"Me gusta resolver problemas matemáticos complejos.", "Ingeniería", "1"},
                    {"Disfruto programar o aprender sobre tecnología.", "Ingeniería", "2"},
                    {"Me interesa el cuerpo humano y la salud.", "Salud", "3"},
                    {"Tengo paciencia para ayudar a personas con problemas.", "Salud", "4"},
                    {"Me gusta liderar equipos y organizar proyectos.", "Negocios", "5"},
                    {"Me interesa entender cómo funcionan las empresas.", "Negocios", "6"},
                    {"Disfruto dibujar, diseñar o crear cosas visuales.", "Arte", "7"},
                    {"Prefiero expresarme de forma creativa antes que técnica.", "Arte", "8"},
                    {"Me gusta debatir y defender ideas con argumentos.", "Legal", "9"},
                    {"Me interesa leer sobre leyes, justicia y derechos.", "Legal", "10"}
            };
            for (String[] row : data) {
                questionRepository.save(Question.builder()
                        .text(row[0])
                        .area(row[1])
                        .orderIndex(Integer.parseInt(row[2]))
                        .build());
            }
            System.out.println("✅ 10 preguntas creadas");
        }
    }

    // =====================================================
    // CURSOS
    // =====================================================
    private void seedCourses() {
        if (courseRepository.count() == 0) {
            courseRepository.save(Course.builder()
                    .title("Python para Principiantes")
                    .provider("Coursera")
                    .url("https://coursera.org")
                    .area("Ingeniería")
                    .skill("Programación")
                    .durationHours(40).build());

            courseRepository.save(Course.builder()
                    .title("Liderazgo y Gestión de Equipos")
                    .provider("edX")
                    .url("https://edx.org")
                    .area("Negocios")
                    .skill("Liderazgo")
                    .durationHours(20).build());

            courseRepository.save(Course.builder()
                    .title("Primeros Auxilios")
                    .provider("Udemy")
                    .url("https://udemy.com")
                    .area("Salud")
                    .skill("Empatía")
                    .durationHours(15).build());

            courseRepository.save(Course.builder()
                    .title("Diseño UX/UI")
                    .provider("Platzi")
                    .url("https://platzi.com")
                    .area("Arte")
                    .skill("Creatividad")
                    .durationHours(30).build());

            courseRepository.save(Course.builder()
                    .title("Introducción al Derecho")
                    .provider("Coursera")
                    .url("https://coursera.org")
                    .area("Legal")
                    .skill("Argumentación")
                    .durationHours(25).build());

            System.out.println("✅ 5 cursos creados");
        }
    }

    // =====================================================
    // UNIVERSIDADES
    // =====================================================
    private void seedUniversities() {
        if (universityRepository.count() == 0) {
            universityRepository.save(University.builder()
                    .name("Universidad Peruana de Ciencias Aplicadas (UPC)")
                    .website("https://upc.edu.pe")
                    .city("Lima")
                    .description("Universidad privada con enfoque innovador")
                    .faculties("Ingeniería,Negocios,Salud,Arte,Legal")
                    .build());

            universityRepository.save(University.builder()
                    .name("Pontificia Universidad Católica del Perú (PUCP)")
                    .website("https://pucp.edu.pe")
                    .city("Lima")
                    .description("Universidad privada de tradición académica")
                    .faculties("Ingeniería,Negocios,Legal")
                    .build());

            System.out.println("✅ 2 universidades creadas");
        }
    }

    // =====================================================
    // 🆕 LOGS INICIALES
    // =====================================================
    private void seedActivityLogs() {
        if (activityLogRepository.count() == 0) {
            activityLogRepository.save(ActivityLog.builder()
                    .action("SYSTEM_START")
                    .description("Sistema iniciado por primera vez - Todos los datos semilla cargados")
                    .username("sistema")
                    .role("SYSTEM")
                    .timestamp(LocalDateTime.now())
                    .ipAddress("localhost")
                    .build());

            System.out.println("✅ Log inicial del sistema creado");
        }
    }
}