package com.example.travel.admin.controller;

import com.example.travel.review.model.Review;
import com.example.travel.review.model.ReviewStatus;
import com.example.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 관리 페이지 - 전체 리뷰 목록 및 필터링
     */
    @GetMapping
    public String reviews(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {

        log.info("리뷰 관리 페이지 접속 - 상태: {}, 카테고리: {}, 평점: {} ~ {}, 검색어: {}, 페이지: {}, 크기: {}",
                status, category, minRating, maxRating, searchTerm, page, size);

        // 정렬 설정 (최신순)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 필터링된 리뷰 목록 조회
        Page<Review> reviewsPage;

        // 신고된 리뷰만 조회
        if ("REPORTED".equals(status)) {
            reviewsPage = reviewService.getReportedReviews(pageable);
        }
        // 상태별 필터링 (승인됨, 대기중, 거부됨)
        else if (!"ALL".equals(status)) {
            ReviewStatus reviewStatus = ReviewStatus.valueOf(status);
            reviewsPage = reviewService.getReviewsByStatus(reviewStatus, pageable);
        }
        // 전체 조회 (추가 필터 적용)
        else {
            reviewsPage = reviewService.getAllReviews(
                    category, minRating, maxRating, searchTerm, pageable);
        }

        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("status", status);
        model.addAttribute("category", category);
        model.addAttribute("minRating", minRating);
        model.addAttribute("maxRating", maxRating);
        model.addAttribute("searchTerm", searchTerm);

        return "admin/reviews";
    }

    /**
     * 신고된 리뷰 목록 조회
     */
    @GetMapping("/reported")
    public String reportedReviews(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("reportCount").descending()
                .and(Sort.by("createdAt").descending()));

        Page<Review> reportedReviewsPage = reviewService.getReportedReviews(pageable);

        model.addAttribute("reviewsPage", reportedReviewsPage);
        model.addAttribute("status", "REPORTED");

        return "admin/reviews";
    }

    /**
     * 리뷰 상세 조회
     */
    @GetMapping("/{id}")
    public String reviewDetail(@PathVariable Long id, Model model) {
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + id));
        model.addAttribute("review", review);
        return "admin/review-detail";
    }

    /**
     * 리뷰 상태 변경 (승인, 거부, 삭제)
     */
    @PostMapping("/{id}/status")
    @ResponseBody
    public String updateReviewStatus(
            @PathVariable Long id,
            @RequestParam ReviewStatus status) {

        log.info("리뷰 {} 상태 변경: {}", id, status);
        reviewService.updateReviewStatus(id, status);

        return "success";
    }

    /**
     * 리뷰 신고 횟수 초기화
     */
    @PostMapping("/{id}/clear-reports")
    @ResponseBody
    public String clearReports(@PathVariable Long id) {
        log.info("리뷰 {} 신고 초기화", id);
        reviewService.clearReportCount(id);
        return "success";
    }

    /**
     * 리뷰 삭제
     */
    @PostMapping("/{id}/delete")
    @ResponseBody
    public String deleteReview(@PathVariable Long id) {
        log.info("리뷰 {} 삭제", id);
        reviewService.deleteReview(id);
        return "success";
    }
}