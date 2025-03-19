package com.example.travel.schedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.schedule.dto.ScheduleDTO;
import com.example.travel.schedule.model.Schedule;
import com.example.travel.schedule.repositroy.ScheduleRepository;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    /**
     * 사용자의 모든 일정을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 모든 일정 목록
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO> getSchedulesByUserId(String userId) {
        log.debug("사용자 ID {}의 모든 일정 조회", userId);
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        return schedules.stream()
                .map(ScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID의 일정을 조회합니다.
     * 
     * @param scheduleId 일정 ID
     * @return 조회된 일정
     * @throws ResourceNotFoundException 일정을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public ScheduleDTO getSchedule(Long scheduleId) {
        log.debug("일정 ID {}의 상세 정보 조회", scheduleId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));
        return ScheduleDTO.fromEntity(schedule);
    }

    /**
     * 특정 사용자의 특정 일정을 조회합니다. (권한 확인 포함)
     * 
     * @param scheduleId 일정 ID
     * @param userId     사용자 ID
     * @return 조회된 일정
     * @throws ResourceNotFoundException 일정을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public ScheduleDTO getScheduleWithAuth(Long scheduleId, String userId) {
        log.debug("사용자 ID {}의 일정 ID {} 조회", userId, scheduleId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));

        // 권한 확인 (본인의 일정만 조회 가능)
        if (!schedule.getUserId().equals(userId)) {
            log.warn("사용자 ID {}가 다른 사용자의 일정 ID {}에 접근 시도", userId, scheduleId);
            throw new ResourceNotFoundException("Schedule", "id", scheduleId);
        }

        return ScheduleDTO.fromEntity(schedule);
    }

    /**
     * 일정을 삭제합니다.
     * 
     * @param scheduleId 삭제할 일정 ID
     * @param userId     사용자 ID (권한 확인용)
     * @throws ResourceNotFoundException 일정을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteSchedule(Long scheduleId, String userId) {
        log.debug("사용자 ID {}의 일정 ID {} 삭제", userId, scheduleId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));

        // 권한 확인 (본인의 일정만 삭제 가능)
        if (!schedule.getUserId().equals(userId)) {
            log.warn("사용자 ID {}가 다른 사용자의 일정 ID {}를 삭제 시도", userId, scheduleId);
            throw new ResourceNotFoundException("Schedule", "id", scheduleId);
        }

        scheduleRepository.delete(schedule);
        log.info("일정 ID {} 삭제 완료", scheduleId);
    }
}
