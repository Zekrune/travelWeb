package com.example.travel.calendar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;
    private String title;
    private String description;

    @Column(nullable = false) // user_id를 필수 값으로 유지
    private Long userId;
}
