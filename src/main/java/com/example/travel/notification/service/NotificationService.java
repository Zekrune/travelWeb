package com.example.travel.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.travel.notification.model.Notification;
import com.example.travel.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Notification createNotification(String message, Long userId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUserId(userId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}
