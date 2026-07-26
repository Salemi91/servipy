package py.com.servipy.shared.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import py.com.servipy.auth.application.JwtService;
import py.com.servipy.user.domain.User;
import py.com.servipy.auth.infrastructure.persistence.UserRepository;
import py.com.servipy.shared.exception.ErrorResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Filtro que intercepta cada request, extrae y valida el JWT,
 * y establece el SecurityContext si el token es válido.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Sin header Authorization → continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Verificar si el token está expirado (para responder TOKEN_EXPIRED)
        if (jwtService.isTokenExpired(token)) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
                "El token de acceso ha expirado");
            return;
        }

        // Verificar si el token es válido (firma, formato)
        if (!jwtService.isTokenValid(token)) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Token de acceso inválido");
            return;
        }

        // Extraer userId y buscar usuario en BD
        Long userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        // Usuario no encontrado o inactivo
        if (user == null || !user.getActive()) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "No autorizado");
            return;
        }

        // Establecer Authentication en SecurityContext
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status,
                                    String code, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now().toString(),
            status.value(),
            code,
            message,
            List.of()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
