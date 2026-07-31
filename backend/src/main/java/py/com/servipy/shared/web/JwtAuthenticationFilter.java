package py.com.servipy.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import py.com.servipy.auth.application.JwtService;
import py.com.servipy.user.domain.User;
import py.com.servipy.user.infrastructure.persistence.UserRepository;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta cada request, extrae y valida el JWT,
 * y establece el SecurityContext si el token es válido.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   ErrorResponseWriter errorResponseWriter) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.errorResponseWriter = errorResponseWriter;
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

        if (jwtService.isTokenExpired(token)) {
            errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
                "El token de acceso ha expirado");
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Token de acceso inválido");
            return;
        }

        Long userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !user.getActive()) {
            errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "No autorizado");
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
