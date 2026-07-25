package py.com.servipy.auth.domain;

/**
 * Roles del sistema ServiPy.
 * CLIENT: busca y solicita servicios.
 * PROFESSIONAL: ofrece servicios, gestiona solicitudes.
 * ADMIN: modera usuarios y contenido.
 */
public enum Role {
    CLIENT,
    PROFESSIONAL,
    ADMIN
}
