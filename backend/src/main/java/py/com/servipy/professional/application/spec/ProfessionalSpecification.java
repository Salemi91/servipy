package py.com.servipy.professional.application.spec;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.OfferedService;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.user.domain.User;

public final class ProfessionalSpecification {

    private ProfessionalSpecification() {
    }

    public static Specification<ProfessionalProfile> build(Long categoryId, String search) {
        return Specification.where(isApproved())
                .and(userIsActive())
                .and(hasActiveServices())
                .and(inCategory(categoryId))
                .and(matchesSearch(search));
    }

    private static Specification<ProfessionalProfile> isApproved() {
        return (root, query, cb) ->
                cb.equal(root.get("approvalStatus"), ApprovalStatus.APPROVED);
    }

    private static Specification<ProfessionalProfile> userIsActive() {
        return (root, query, cb) -> {
            Join<ProfessionalProfile, User> user = root.join("user");
            return cb.isTrue(user.get("active"));
        };
    }

    private static Specification<ProfessionalProfile> hasActiveServices() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<OfferedService> service = subquery.from(OfferedService.class);
            subquery.select(service.get("id"))
                    .where(
                            cb.equal(service.get("professional"), root),
                            cb.isTrue(service.get("active"))
                    );
            return cb.exists(subquery);
        };
    }

    private static Specification<ProfessionalProfile> inCategory(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<OfferedService> service = subquery.from(OfferedService.class);
            subquery.select(service.get("id"))
                    .where(
                            cb.equal(service.get("professional"), root),
                            cb.isTrue(service.get("active")),
                            cb.equal(service.get("category").get("id"), categoryId)
                    );
            return cb.exists(subquery);
        };
    }

    private static Specification<ProfessionalProfile> matchesSearch(String search) {
        if (search == null || search.isBlank()) return null;
        return (root, query, cb) -> {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            Join<ProfessionalProfile, User> user = root.join("user");
            Predicate nameLike = cb.like(cb.lower(user.get("name")), pattern);
            Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<OfferedService> service = subquery.from(OfferedService.class);
            subquery.select(service.get("id"))
                    .where(
                            cb.equal(service.get("professional"), root),
                            cb.isTrue(service.get("active")),
                            cb.like(cb.lower(service.get("name")), pattern)
                    );
            Predicate serviceLike = cb.exists(subquery);

            return cb.or(nameLike, descLike, serviceLike);
        };
    }
}
