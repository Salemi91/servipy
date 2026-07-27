package py.com.servipy.client.application.dto;

import java.util.List;

public record ServiceRequestPageResponse(
    List<ServiceRequestResponse> content,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {}
