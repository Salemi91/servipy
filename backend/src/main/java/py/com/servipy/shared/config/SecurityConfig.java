package py.com.servipy.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import py.com.servipy.shared.web.JwtAuthenticationFilter;
import py.com.servipy.shared.web.RestAccessDeniedHandler;
import py.com.servipy.shared.web.RestAuthenticationEntryPoint;

import java.util.List;

/**
 * Configuración de Spring Security con JWT y CORS.
 * Define endpoints públicos, reglas de autorización por rol,
 * registra el filtro JWT y configura CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/v1/health").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/register/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Creación de solicitudes: público (formulario del perfil del profesional)
                .requestMatchers(HttpMethod.POST, "/api/v1/professionals/*/service-requests").permitAll()
                // El resto de la gestión de solicitudes es privativa del profesional.
                // Debe declararse antes del catálogo para no quedar cubierta por su comodín.
                .requestMatchers("/api/v1/professionals/*/service-requests/**").authenticated()
                .requestMatchers("/api/v1/professionals/*/service-requests").authenticated()
                // Endpoints públicos de catálogo
                .requestMatchers(HttpMethod.GET, "/api/v1/professionals/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/cities").permitAll()
                // Endpoints de admin
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
