package pe.edu.upc.careerpath_ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    // ==========================================================
    // CADENA 1: API REST (stateless, JWT)
    // ==========================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(RestSecurityHandlers.entryPoint())
                .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/users/me").hasRole("ESTUDIANTE")
                .requestMatchers("/api/users/**").hasAnyRole("DOCENTE", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==========================================================
    // CADENA 2: WEB (Thymeleaf + formLogin)
    // ==========================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // -------- Públicos --------
                .requestMatchers("/", "/login", "/register", "/contact",
                                 "/newsletter/**", "/share/**",
                                 "/institution/register",
                                 "/css/**", "/js/**", "/images/**").permitAll()

                // -------- Roles --------
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/institution/**").hasRole("COLEGIO")
                .requestMatchers("/university/**").hasRole("UNIVERSIDAD")
                .requestMatchers("/student/**").hasRole("ESTUDIANTE")
                .requestMatchers("/teacher/**").hasRole("DOCENTE")

                // -------- Comparaciones (cualquier autenticado) --------
                .requestMatchers("/careers/compare", "/compare").authenticated()

                // -------- Lectura de catálogos --------
                .requestMatchers(HttpMethod.GET,
                    "/careers", "/careers/detail/**",
                    "/universities", "/universities/detail/**").authenticated()

                // -------- Escritura de catálogos --------
                .requestMatchers("/careers/**").hasAnyRole("DOCENTE", "ADMIN")
                .requestMatchers("/universities/**").hasAnyRole("DOCENTE", "ADMIN")

                // -------- Perfil --------
                .requestMatchers("/profile/**").authenticated()

                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler((request, response, exception) ->
                        response.sendRedirect(exception instanceof DisabledException
                                ? "/login?suspended" : "/login?error"))
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());

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