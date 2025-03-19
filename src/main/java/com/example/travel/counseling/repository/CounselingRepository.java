package com.example.travel.counseling.repository;

import com.example.travel.counseling.model.Counseling;
import com.example.travel.counseling.model.CounselingCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounselingRepository extends JpaRepository<Counseling, Long> {

    // 특정 사용자의 모든 문의 조회 (페이징)
    Page<Counseling> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 특정 사용자의 모든 문의 조회 (리스트)
    List<Counseling> findByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 ID와 사용자 ID로 문의 조회
    Optional<Counseling> findByIdAndUserId(Long id, String userId);

    // 답변 여부에 따른 문의 조회
    Page<Counseling> findByIsAnsweredOrderByCreatedAtDesc(boolean isAnswered, Pageable pageable);

    // 특정 사용자의 답변 여부에 따른 문의 조회
    Page<Counseling> findByUserIdAndIsAnsweredOrderByCreatedAtDesc(String userId, boolean isAnswered,
            Pageable pageable);

    // 카테고리별 문의 조회
    Page<Counseling> findByCategoryOrderByCreatedAtDesc(CounselingCategory category, Pageable pageable);

    // 특정 사용자의 카테고리별 문의 조회
    Page<Counseling> findByUserIdAndCategoryOrderByCreatedAtDesc(String userId, CounselingCategory category,
            Pageable pageable);

    // 미답변 문의 수 조회
    long countByIsAnswered(boolean isAnswered);

    // 카테고리별 문의 수 조회
    long countByCategory(CounselingCategory category);

    // 사용자별 문의 수 조회
    long countByUserId(String userId);

    // 사용자의 미답변 문의 수 조회
    long countByUserIdAndIsAnswered(String userId, boolean isAnswered);

    // 최근 문의 목록 조회
    @Query(value = "SELECT c FROM Counseling c ORDER BY c.createdAt DESC LIMIT :limit")
    List<Counseling> findTopByOrderByCreatedAtDesc(@Param("limit") int limit);

    // 월별 문의 통계
    @Query(value = "SELECT MONTH(created_at) as month, COUNT(*) as count FROM counselings " +
            "WHERE YEAR(created_at) = :year GROUP BY MONTH(created_at)", nativeQuery = true)
    List<Object[]> getMonthlyStatsByYear(@Param("year") int year);

    // 제목이나 내용으로 검색 (특정 사용자)
    Page<Counseling> findByUserIdAndTitleContainingOrUserIdAndContentContaining(
            String userId1, String title, String userId2, String content, Pageable pageable);

    // 전체 키워드 검색 (관리자용)
    @Query("SELECT c FROM Counseling c WHERE c.title LIKE %:keyword% OR c.content LIKE %:keyword%")
    Page<Counseling> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}