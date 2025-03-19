package com.example.travel.notification.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private Long userId; // 알림을 받을 사용자 ID
    private LocalDateTime createdAt; // 알림 생성 시간
    private boolean isRead; // 읽음 여부
}
