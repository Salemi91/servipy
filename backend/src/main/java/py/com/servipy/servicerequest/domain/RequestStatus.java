package py.com.servipy.servicerequest.domain;

public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    /**
     * Valida si la transición de estado es permitida.
     * Solo PENDING puede transicionar a ACCEPTED o REJECTED.
     */
    public boolean canTransitionTo(RequestStatus target) {
        if (this != PENDING) return false;
        return target == ACCEPTED || target == REJECTED;
    }
}
