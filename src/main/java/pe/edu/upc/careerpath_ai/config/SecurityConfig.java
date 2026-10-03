package pe.edu.upc.careerpath_ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upc.careerpath_ai.security.JwtAuthenticationFilter;
import pe.edu.upc.careerpath_ai.security.JwtService;
import pe.edu.upc.careerpath_ai.security.RestSecurityHandlers;

@Configuration
@EnableMethodSecurity   // 🆕 Necesario para @PreAuthorize en controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    // =====================================================
    // API REST (/api/**): stateless, JWT, errores en JSON
    // =====================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/careers/**").authenticated()
                .requestMatchers("/api/careers/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("DOCENTE", "ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(RestSecurityHandlers.entryPoint())
                .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler())
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================
    // Aplicación web (Thymeleaf)
    // =====================================================
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth

                // ===== Rutas públicas =====
                .requestMatchers("/", "/register", "/login", "/contact",
                                 "/newsletter/**",
                                 "/institution/register",
                                 "/share/**",                        // 🆕 Compartir resultados
                                 "/css/**", "/js/**", "/images/**",
                                 "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // ===== Universidades: LECTURA pública =====
                .requestMatchers(HttpMethod.GET, "/universities",
                                               "/universities/detail/**").permitAll()

                // ===== Universidades: CRUD solo ADMIN =====
                .requestMatchers("/universities/new",
                                 "/universities/edit/**",
                                 "/universities/delete/**",
                                 "/universities/update/**").hasRole("ADMIN")
                .requestMatchers("/universities/**").authenticated()

                // ===== Carreras: LECTURA para cualquier autenticado =====
                .requestMatchers("/careers/compare", "/careers/compare/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/careers",
                                               "/careers/detail/**").authenticated()

                // ===== Carreras: CRUD solo ADMIN =====
                .requestMatchers("/careers/new",
                                 "/careers/edit/**",
                                 "/careers/delete/**",
                                 "/careers/update/**").hasRole("ADMIN")
                .requestMatchers("/careers/**").hasRole("ADMIN")

                // ===== Por rol =====
                .requestMatchers("/student/**").hasRole("ESTUDIANTE")
                .requestMatchers("/teacher/**").hasRole("DOCENTE")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/institution/**").hasRole("COLEGIO")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}