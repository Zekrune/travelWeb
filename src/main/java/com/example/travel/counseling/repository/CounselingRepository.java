package com.example.travel.counseling.repository;

import com.example.travel.counseling.model.Counseling;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    Page<Counseling> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    
    // 미답변 문의 수 조회
    long countByIsAnswered(boolean isAnswered);
} 