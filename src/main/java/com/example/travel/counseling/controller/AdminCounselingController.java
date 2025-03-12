package com.example.travel.counseling.controller;

import com.example.travel.counseling.model.Counseling;
import com.example.travel.counseling.model.CounselingCategory;
import com.example.travel.counseling.service.CounselingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/counseling")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCounselingController {

    private final CounselingService counselingService;

    /**
     * 관리자 문의 목록 페이지
     */
    @GetMapping
    public String adminCounselingList(
            @RequestParam(value = "answered", required = false) Boolean answered,
            @RequestParam(value = "category", required = false) CounselingCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<Counseling> counselings;

        if (answered != null) {
            counselings = counselingService.getCounselingsByAnswerStatus(answered, pageable);
            model.addAttribute("answered", answered);
        } else if (category != null) {
            counselings = counselingService.getCounselingsByCategory(category, pageable);
            model.addAttribute("selectedCategory", category);
        } else {
            counselings = counselingService.getAllCounselings(pageable);
        }

        model.addAttribute("counselings", counselings);
        model.addAttribute("categories", CounselingCategory.values());
        model.addAttribute("unansweredCount", counselingService.getUnansweredCounselingCount());

        return "admin/counseling/list";
    }

    /**
     * 관리자 문의 상세 페이지
     */
    @GetMapping("/{id}")
    public String adminCounselingDetail(@PathVariable("id") Long id, Model model) {
        Optional<Counseling> optionalCounseling = counselingService.getCounselingById(id);

        if (optionalCounseling.isPresent()) {
            model.addAttribute("counseling", optionalCounseling.get());
            return "admin/counseling/detail";
        } else {
            return "redirect:/admin/counseling?error=not_found";
        }
    }

    /**
     * 문의 답변 처리
     */
    @PostMapping("/{id}/answer")
    public String answerCounseling(
            @PathVariable("id") Long id,
            @RequestParam("answer") String answer,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String adminId = authentication.getName();

        try {
            counselingService.answerCounseling(id, answer, adminId);
            redirectAttributes.addFlashAttribute("message", "답변이 성공적으로 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/counseling/" + id;
    }

    /**
     * 문의 삭제 처리
     */
    @PostMapping("/{id}/delete")
    public String deleteCounseling(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            counselingService.deleteCounseling(id);
            redirectAttributes.addFlashAttribute("message", "문의가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "문의 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/counseling";
    }
}