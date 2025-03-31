package com.example.travel.review.repository;

import com.example.travel.review.model.Review;
import com.example.travel.review.model.ReviewStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 고급 검색을 위한 커스텀 리포지토리 구현체
 */
@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 다양한 조건으로 리뷰 필터링 검색
     */
    @Override
    public Page<Review> findWithFilters(
            String category,
            Double minRating,
            Double maxRating,
            String searchTerm,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = cb.createQuery(Review.class);
        Root<Review> root = query.from(Review.class);
        
        // 조건 목록 생성
        List<Predicate> predicates = new ArrayList<>();
        
        // 기본 조건: 삭제되지 않은 리뷰만 조회
        predicates.add(cb.notEqual(root.get("status"), ReviewStatus.DELETED));
        
        // 카테고리 필터
        if (StringUtils.hasText(category)) {
            predicates.add(cb.equal(root.get("category"), category));
        }
        
        // 평점 범위 필터
        if (minRating != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
        }
        if (maxRating != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("rating"), maxRating));
        }
        
        // 검색어 필터 (제목 또는 내용에 포함)
        if (StringUtils.hasText(searchTerm)) {
            String likePattern = "%" + searchTerm + "%";
            Predicate titlePredicate = cb.like(root.get("title"), likePattern);
            Predicate contentPredicate = cb.like(root.get("content"), likePattern);
            predicates.add(cb.or(titlePredicate, contentPredicate));
        }
        
        // 조건 적용
        query.where(predicates.toArray(new Predicate[0]));
        
        // 정렬 적용
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(sort -> {
                if (sort.isAscending()) {
                    orders.add(cb.asc(root.get(sort.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(sort.getProperty())));
                }
            });
            query.orderBy(orders);
        } else {
            // 기본 정렬: 최신순
            query.orderBy(cb.desc(root.get("createdAt")));
        }
        
        // 쿼리 실행
        TypedQuery<Review> typedQuery = entityManager.createQuery(query);
        
        // 페이징 적용
        int totalRows = typedQuery.getResultList().size();
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        // 결과 반환
        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }
} 