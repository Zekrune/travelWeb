package com.example.travel.attraction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.travel.attraction.model.Attraction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    // 기본 조회 메서드
    List<Attraction> findByGugunNm(String gugunNm);

    Page<Attraction> findByGugunNm(String gugunNm, Pageable pageable);

    List<Attraction> findTop10ByOrderByIdDesc();

    // UC_SEQ로 조회 (중복 체크용)
    Optional<Attraction> findByUcSeq(String ucSeq);

    boolean existsByUcSeq(String ucSeq);

    // 메인 타이틀로 검색
    Page<Attraction> findByMainTitleContaining(String keyword, Pageable pageable);

    // 구군명과 메인 타이틀로 검색
    Page<Attraction> findByGugunNmAndMainTitleContaining(String gugunNm, String keyword, Pageable pageable);

    // 최근 업데이트된 명소 조회
    List<Attraction> findTop20ByOrderByUpdatedAtDesc();

    // 특정 시간 이후에 업데이트된 명소 수 조회
    @Query("SELECT COUNT(a) FROM Attraction a WHERE a.updatedAt > ?1")
    long countByUpdatedAtAfter(LocalDateTime dateTime);

    // 인기 지역 명소 조회 (구군별 개수)
    @Query("SELECT a.gugunNm, COUNT(a) FROM Attraction a GROUP BY a.gugunNm ORDER BY COUNT(a) DESC")
    List<Object[]> countByGugunNmGroupByGugunNm();
}