package com.example.travel.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.travel.notification.model.Notification;
import com.example.travel.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification createdNotification = notificationService.createNotification(notification.getMessage(), notification.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}