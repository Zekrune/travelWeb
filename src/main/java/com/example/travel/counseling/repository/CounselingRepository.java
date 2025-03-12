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

@Repository
public interface CounselingRepository extends JpaRepository<Counseling, Long> {

    // 특정 사용자의 모든 문의 조회 (페이징)
    Page<Counseling> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 특정 사용자의 모든 문의 조회 (리스트)
    List<Counseling> findByUserIdOrderByCreatedAtDesc(String userId);

    // 답변 여부에 따른 문의 조회
    Page<Counseling> findByIsAnsweredOrderByCreatedAtDesc(boolean isAnswered, Pageable pageable);

    // 카테고리별 문의 조회
    Page<Counseling> findByCategoryOrderByCreatedAtDesc(CounselingCategory category, Pageable pageable);

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
}