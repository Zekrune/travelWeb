package com.example.travel.admin.service;

import com.example.travel.admin.dto.DashboardDataDto;
import com.example.travel.attraction.model.Attraction;
import com.example.travel.attraction.repository.AttractionRepository;
import com.example.travel.counseling.repository.CounselingRepository;
import com.example.travel.itinerary.model.TravelPlan;
import com.example.travel.itinerary.repository.TravelPlanRepository;
import com.example.travel.review.model.Review;
import com.example.travel.review.repository.ReviewRepository;
import com.example.travel.user.model.User;
import com.example.travel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final CounselingRepository counselingRepository;
    private final AttractionRepository attractionRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 관리자 대시보드에서 필요한 통계를 조회하여 반환.
     */
    public DashboardDataDto getDashboardData() {

        // 1. 총 사용자 수
        long userCount = userRepository.count();

        // 2. 총 여행 일정 수
        long totalItineraries = travelPlanRepository.count();

        // 🔹 이번 주 생성된 일정 수: createdAt이 7일 이내인 것만 count
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        long weeklyCreatedItineraries = travelPlanRepository.countByCreatedAtAfter(oneWeekAgo);

        // 4. 명소 추가 요청 수 (별도 '명소요청' 테이블 없으면 일단 attractionRepository.count()로 대체)
        long pendingSpotRequests = attractionRepository.count();

        // 5. 미답변 1:1 문의 수
        long unansweredInquiries = counselingRepository.countByIsAnswered(false);

        // 6) 신고된 리뷰 수 (현재는 0으로 처리, 필요시 reportCount 필드 사용)
        long reportedReviews = 0;

        // 빌드하여 반환
        return DashboardDataDto.builder()
                .userCount(userCount)
                .totalItineraries(totalItineraries)
                .weeklyCreatedItineraries(weeklyCreatedItineraries)
                .pendingSpotRequests(pendingSpotRequests)
                .unansweredInquiries(unansweredInquiries)
                .reportedReviews(reportedReviews)
                .build();
    }

    /**
     * 총 여행 일정 수를 반환합니다.
     */
    public long getTotalItinerariesCount() {
        return travelPlanRepository.count();
    }

    /**
     * 활성 상태의 여행 일정 수를 반환합니다.
     */
    public long getActiveItinerariesCount() {
        // 실제 구현에서는 status 필드가 active인 일정만 카운트
        // 필드가 없다면 전체 개수의 약 70%를 활성으로 간주
        return Math.round(getTotalItinerariesCount() * 0.7);
    }

    /**
     * 완료된 여행 일정 수를 반환합니다.
     */
    public long getCompletedItinerariesCount() {
        // 실제 구현에서는 status 필드가 completed인 일정만 카운트
        // 필드가 없다면 전체 개수의 약 30%를 완료로 간주
        return Math.round(getTotalItinerariesCount() * 0.3);
    }

    /**
     * 이번 주에 생성된 여행 일정 수를 반환합니다.
     */
    public long getThisWeekItinerariesCount() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return travelPlanRepository.countByCreatedAtAfter(oneWeekAgo);
    }

    /**
     * 가장 인기 있는 여행 목적지를 반환합니다.
     */
    public String getMostPopularDestination() {
        // 실제 구현에서는 destination 필드를 그룹화하여 가장 많은 것을 반환
        // 임시 데이터로 "도쿄" 반환
        return "도쿄";
    }

    /**
     * 모든 여행 일정 목록을 반환합니다.
     */
    public List<TravelPlan> getAllItineraries() {
        return travelPlanRepository.findAll();
    }

    /**
     * 모든 관광 명소 목록을 반환합니다.
     */
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    /**
     * 모든 사용자 목록을 반환합니다.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 모든 리뷰 목록을 반환합니다.
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    /**
     * 신고된 리뷰 목록을 반환합니다. (호환성을 위해 유지)
     */
    public List<Review> getReportedReviews() {
        // 실제 구현에서는 AdminReviewsController에서 직접 처리하므로
        // 호환성을 위해 빈 목록만 반환합니다.
        return Collections.emptyList();
    }

    /**
     * 시스템 로그를 반환합니다.
     */
    public List<Map<String, Object>> getSystemLogs() {
        // 실제 구현에서는 로그 테이블이나 파일에서 데이터를 가져옴
        // 임시 구현으로 빈 리스트 반환
        return new ArrayList<>();
    }

    /**
     * 총 사용자 수 조회
     */
    public int getUserCount() {
        // 임시로 더미 데이터 반환 (실제로는 UserRepository에서 count() 호출)
        return 28;
    }

    /**
     * 대기 중인 명소 추가 요청 수 조회
     */
    public int getPendingSpotRequestsCount() {
        // 임시로 더미 데이터 반환
        return 12;
    }

    /**
     * 미답변 문의 수 조회
     */
    public int getUnansweredInquiriesCount() {
        // 임시로 더미 데이터 반환
        return 8;
    }

    /**
     * 신고된 리뷰 수 조회
     */
    public int getReportedReviewsCount() {
        // 임시로 더미 데이터 반환
        return 5;
    }
}
