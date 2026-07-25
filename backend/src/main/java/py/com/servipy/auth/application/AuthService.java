package py.com.servipy.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import py.com.servipy.auth.domain.Role;
import py.com.servipy.auth.domain.User;
import py.com.servipy.auth.infrastructure.persistence.UserRepository;
import py.com.servipy.auth.infrastructure.web.AuthResponse;
import py.com.servipy.auth.infrastructure.web.LoginRequest;
import py.com.servipy.auth.infrastructure.web.RegisterRequest;
import py.com.servipy.auth.infrastructure.web.UserResponse;
import py.com.servipy.shared.exception.AccountInactiveException;
import py.com.servipy.shared.exception.DuplicateEmailException;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Servicio de aplicación para registro y autenticación de usuarios.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registra un nuevo usuario con rol CLIENT.
     */
    public AuthResponse registerClient(RegisterRequest request) {
        return register(request, Role.CLIENT);
    }

    /**
     * Registra un nuevo usuario con rol PROFESSIONAL.
     */
    public AuthResponse registerProfessional(RegisterRequest request) {
        return register(request, Role.PROFESSIONAL);
    }

    /**
     * Autentica un usuario por email y password.
     * Retorna AuthResponse con JWT si las credenciales son válidas.
     *
     * @throws BadCredentialsException si el email no existe o el password no coincide
     * @throws AccountInactiveException si la cuenta está inactiva
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!user.getActive()) {
            throw new AccountInactiveException();
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.bearer(token, UserResponse.from(user));
    }

    private AuthResponse register(RegisterRequest request, Role role) {
        String trimmedName = request.name().trim();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(trimmedName, normalizedEmail, passwordHash, role);
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.bearer(token, UserResponse.from(user));
    }
}
