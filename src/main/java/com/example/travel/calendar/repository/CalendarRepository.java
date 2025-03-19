package com.example.travel.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.travel.calendar.model.Calendar;

import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    List<Calendar> findByDate(String date);
}
