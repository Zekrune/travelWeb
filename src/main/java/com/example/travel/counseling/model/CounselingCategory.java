package com.example.travel.counseling.model;

public enum CounselingCategory {
    GENERAL("일반 문의"),
    ACCOUNT("계정 문의"),
    PAYMENT("결제 문의"),
    TRAVEL_PLAN("여행 계획 문의"),
    ATTRACTION("관광지 문의"),
    SYSTEM("시스템 오류"),
    OTHER("기타");

    private final String displayName;

    CounselingCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}