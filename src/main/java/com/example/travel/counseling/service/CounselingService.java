package com.example.travel.counseling.service;

import com.example.travel.counseling.model.Counseling;
import com.example.travel.counseling.model.CounselingCategory;
import com.example.travel.counseling.repository.CounselingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounselingService {

    private final CounselingRepository counselingRepository;

    /**
     * 새로운 문의 생성
     */
    @Transactional
    public Counseling createCounseling(String userId, String title, String content,
            CounselingCategory category) {
        Counseling counseling = Counseling.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .category(category)
                .isAnswered(false)
                .createdAt(LocalDateTime.now())
                .build();

        return counselingRepository.save(counseling);
    }

    /**
     * 문의 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<Counseling> getCounselingById(Long id) {
        return counselingRepository.findById(id);
    }

    /**
     * 특정 사용자의 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Counseling> getCounselingsByUserId(String userId, Pageable pageable) {
        return counselingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 특정 사용자의 특정 카테고리 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Counseling> getCounselingsByUserIdAndCategory(String userId, CounselingCategory category,
            Pageable pageable) {
        return counselingRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable);
    }

    /**
     * 특정 사용자의 답변 상태별 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Counseling> getCounselingsByUserIdAndAnswerStatus(String userId, boolean isAnswered,
            Pageable pageable) {
        return counselingRepository.findByUserIdAndIsAnsweredOrderByCreatedAtDesc(userId, isAnswered, pageable);
    }

    /**
     * 모든 문의 목록 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Page<Counseling> getAllCounselings(Pageable pageable) {
        return counselingRepository.findAll(pageable);
    }

    /**
     * 답변 여부에 따른 문의 목록 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Page<Counseling> getCounselingsByAnswerStatus(boolean isAnswered, Pageable pageable) {
        return counselingRepository.findByIsAnsweredOrderByCreatedAtDesc(isAnswered, pageable);
    }

    /**
     * 문의 답변 등록 (관리자용)
     */
    @Transactional
    public Counseling answerCounseling(Long id, String answer, String adminId) {
        Optional<Counseling> optionalCounseling = counselingRepository.findById(id);

        if (optionalCounseling.isPresent()) {
            Counseling counseling = optionalCounseling.get();
            counseling.setAnswer(answer);
            counseling.setAnsweredBy(adminId);
            counseling.setAnsweredAt(LocalDateTime.now());
            counseling.setAnswered(true);

            return counselingRepository.save(counseling);
        } else {
            throw new IllegalArgumentException("해당 ID의 문의가 존재하지 않습니다: " + id);
        }
    }

    /**
     * 미답변 문의 수 조회 (관리자용 대시보드)
     */
    @Transactional(readOnly = true)
    public long getUnansweredCounselingCount() {
        return counselingRepository.countByIsAnswered(false);
    }

    /**
     * 문의 삭제
     */
    @Transactional
    public void deleteCounseling(Long id) {
        counselingRepository.deleteById(id);
    }

    /**
     * 특정 사용자의 문의 삭제
     */
    @Transactional
    public boolean deleteCounselingByIdAndUserId(Long id, String userId) {
        Optional<Counseling> counseling = counselingRepository.findByIdAndUserId(id, userId);
        if (counseling.isPresent()) {
            counselingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 카테고리별 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Counseling> getCounselingsByCategory(CounselingCategory category, Pageable pageable) {
        return counselingRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
    }

    /**
     * 최근 문의 목록 조회 (관리자 대시보드용)
     */
    @Transactional(readOnly = true)
    public List<Counseling> getRecentCounselings(int limit) {
        return counselingRepository.findTopByOrderByCreatedAtDesc(limit);
    }

    /**
     * 카테고리별 문의 수 통계 (관리자 대시보드용)
     */
    @Transactional(readOnly = true)
    public long countByCategory(CounselingCategory category) {
        return counselingRepository.countByCategory(category);
    }

    /**
     * 사용자별 문의 수 조회
     */
    @Transactional(readOnly = true)
    public long countByUserId(String userId) {
        return counselingRepository.countByUserId(userId);
    }

    /**
     * 사용자의 미답변 문의 수 조회
     */
    @Transactional(readOnly = true)
    public long countByUserIdAndIsAnswered(String userId, boolean isAnswered) {
        return counselingRepository.countByUserIdAndIsAnswered(userId, isAnswered);
    }

    /**
     * 제목이나 내용으로 검색
     */
    @Transactional(readOnly = true)
    public Page<Counseling> searchCounselingsByUserId(String userId, String keyword, Pageable pageable) {
        return counselingRepository.findByUserIdAndTitleContainingOrUserIdAndContentContaining(
                userId, keyword, userId, keyword, pageable);
    }
}