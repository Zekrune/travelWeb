package com.example.travel.counseling.controller;

import com.example.travel.counseling.model.Counseling;
import com.example.travel.counseling.model.CounselingCategory;
import com.example.travel.counseling.service.CounselingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/counseling")
public class CounselingController {

    private final CounselingService counselingService;

    /**
     * 사용자 문의 목록 페이지
     */
    @GetMapping
    public String counselingList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            Model model) {

        String userId = authentication.getName();
        Page<Counseling> counselings = counselingService.getCounselingsByUserId(userId, pageable);

        model.addAttribute("counselings", counselings);
        model.addAttribute("categories", CounselingCategory.values());

        return "counseling/list";
    }

    /**
     * 문의 작성 페이지
     */
    @GetMapping("/create")
    public String createCounselingForm(Model model) {
        model.addAttribute("categories", CounselingCategory.values());
        return "counseling/create";
    }

    /**
     * 문의 작성 처리
     */
    @PostMapping("/create")
    public String createCounseling(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") CounselingCategory category,
            @RequestParam(value = "isPrivate", defaultValue = "false") boolean isPrivate,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String userId = authentication.getName();

        Counseling counseling = counselingService.createCounseling(userId, title, content, category, isPrivate);

        redirectAttributes.addFlashAttribute("message", "문의가 성공적으로 등록되었습니다.");
        return "redirect:/counseling";
    }

    /**
     * 문의 상세 페이지
     */
    @GetMapping("/{id}")
    public String counselingDetail(@PathVariable("id") Long id, Authentication authentication, Model model) {
        String userId = authentication.getName();
        Optional<Counseling> optionalCounseling = counselingService.getCounselingById(id);

        if (optionalCounseling.isPresent()) {
            Counseling counseling = optionalCounseling.get();

            // 본인 문의가 아니고, 관리자도 아닌 경우 접근 제한
            if (!counseling.getUserId().equals(userId) && !authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/counseling?error=unauthorized";
            }

            model.addAttribute("counseling", counseling);
            return "counseling/detail";
        } else {
            return "redirect:/counseling?error=not_found";
        }
    }
}