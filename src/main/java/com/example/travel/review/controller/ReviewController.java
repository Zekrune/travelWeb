package com.example.travel.review.controller;

import com.example.travel.review.dto.ReviewDTO;
import com.example.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 목록 페이지
     */
    @GetMapping("/list")
    public String listReviews(
            @RequestParam(defaultValue = "") String destination,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model, Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviewsPage;

        // 정렬 조건에 따라 다른 메서드 호출
        switch (sort) {
            case "rating":
                reviewsPage = reviewService.getReviewsByDestinationOrderByRating(destination, pageable, authentication);
                break;
            case "popular":
                reviewsPage = reviewService.getReviewsByDestinationOrderByPopularity(destination, pageable,
                        authentication);
                break;
            default: // recent
                reviewsPage = reviewService.getReviewsByDestination(destination, pageable, authentication);
                break;
        }

        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("destination", destination);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);

        return "reviews/reviewsList";
    }

    /**
     * 리뷰 상세 페이지
     */
    @GetMapping("/{reviewId}")
    public String viewReview(@PathVariable(name = "reviewId") Long reviewId, Model model, Authentication authentication) {
        ReviewDTO review = reviewService.getReviewDetail(reviewId, authentication);
        model.addAttribute("review", review);

        return "reviews/reviewDetail";
    }

    /**
     * 리뷰 저장 처리
     */
    @PostMapping("/save")
    public String saveReview(
            @ModelAttribute ReviewDTO reviewDTO,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "tags", required = false) String tagsString,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("리뷰 저장 요청: {}", reviewDTO);

            // 태그 처리
            if (tagsString != null && !tagsString.trim().isEmpty()) {
                Set<String> tags = Arrays.stream(tagsString.split(","))
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .collect(Collectors.toSet());
                reviewDTO.setTags(tags);
            }

            // 이미지가 null이 아닌 경우에만 처리
            List<MultipartFile> validPhotos = new ArrayList<>();
            if (photos != null) {
                validPhotos = photos.stream()
                        .filter(photo -> photo != null && !photo.isEmpty())
                        .collect(Collectors.toList());
            }

            ReviewDTO savedReview = reviewService.saveReview(reviewDTO, validPhotos, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 등록되었습니다.");
            return "redirect:/reviews/" + savedReview.getId();
        } catch (Exception e) {
            log.error("리뷰 저장 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 저장 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/schedule/schedule";
        }
    }

    /**
     * 리뷰 수정 페이지
     */
    @GetMapping("/edit/{reviewId}")
    public String editReviewForm(@PathVariable(name = "reviewId") Long reviewId, Model model, Authentication authentication) {
        ReviewDTO review = reviewService.getReviewDetail(reviewId, authentication);

        // 작성자가 아니면 상세 페이지로 리다이렉트
        if (!review.isAuthor()) {
            return "redirect:/reviews/" + reviewId;
        }

        model.addAttribute("review", review);
        // 태그를 쉼표로 구분된 문자열로 변환
        model.addAttribute("tagString", String.join(",", review.getTags()));

        return "reviews/reviewEdit";
    }

    /**
     * 리뷰 수정 처리
     */
    @PostMapping("/update/{reviewId}")
    public String updateReview(
            @PathVariable(name = "reviewId") Long reviewId,
            @ModelAttribute ReviewDTO reviewDTO,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(name = "tags") String tagString,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // 태그 처리
            if (tagString != null && !tagString.isEmpty()) {
                reviewDTO.setTags(Arrays.stream(tagString.split(","))
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .collect(Collectors.toSet()));
            }

            ReviewDTO updatedReview = reviewService.updateReview(reviewId, reviewDTO, photos, authentication);
            redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 수정되었습니다.");

            return "redirect:/reviews/" + updatedReview.getId();
        } catch (Exception e) {
            log.error("리뷰 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/reviews/edit/" + reviewId;
        }
    }

    /**
     * 리뷰 삭제 처리
     */
    @DeleteMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable(name = "reviewId") Long reviewId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            reviewService.deleteReviewByUser(reviewId, authentication);
            response.put("success", true);
            response.put("message", "리뷰가 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 삭제 처리
     */
    @PostMapping("/{reviewId}/delete")
    public String deleteReviewMvc(
            @PathVariable(name = "reviewId") Long reviewId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.deleteReviewByUser(reviewId, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 삭제되었습니다.");
            return "redirect:/reviews/list";
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/reviews/" + reviewId;
        }
    }

    /**
     * 리뷰 작성 가능 여부 확인
     */
    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkCanReview(
            @RequestParam(name = "scheduleId") Long scheduleId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        boolean canReview = reviewService.canReviewSchedule(scheduleId, authentication);

        response.put("canReview", canReview);
        return ResponseEntity.ok(response);
    }

    /**
     * 키워드로 리뷰 검색
     */
    @GetMapping("/search")
    public String searchReviews(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(defaultValue = "") String destination,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model, Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviewsPage = reviewService.searchReviews(keyword, destination, pageable, authentication);

        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("destination", destination);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);

        return "reviews/reviewsList";
    }

    /**
     * 태그로 리뷰 검색
     */
    @GetMapping("/tags")
    public String searchReviewsByTags(
            @RequestParam(name = "tags") String tags,
            @RequestParam(defaultValue = "") String destination,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model, Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviewsPage = reviewService.searchReviewsByTags(tags, destination, pageable, authentication);

        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("destination", destination);
        model.addAttribute("tags", tags);
        model.addAttribute("currentPage", page);

        return "reviews/reviewsList";
    }

    /**
     * 사용자가 작성한 리뷰 목록
     */
    @GetMapping("/my-reviews")
    public String myReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviewsPage = reviewService.getUserReviews(authentication, pageable);

        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("currentPage", page);

        return "reviews/myReviews";
    }
}