package com.mefi.backend.db.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "private_schedule")
@Getter
@NoArgsConstructor
public class PrivateSchedule {
    // 일정식별ID
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임하는 전략
    private Long id;

    // 시작시간
    private LocalDateTime startedTime;

    // 종료시간
    private LocalDateTime endTime;

    // 개인일정
    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    // 요약
    private String summary;

    // 상세 설명
    private String description;

    // 유저와 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public PrivateSchedule(LocalDateTime startedTime, LocalDateTime endTime, ScheduleType type, String summary, String description, User user){

        this.startedTime = startedTime;
        this.endTime = endTime;
        this.type = type;
        this.summary = summary;
        this.description = description;
        this.user = user;
    }

    // 개인 일정 정보 수정 메서드
    public void changeDetail(String summary, String description, LocalDateTime startedTime, LocalDateTime endTime) {
        this.startedTime = startedTime;
        this.endTime = endTime;
        this.summary = summary;
        this.description = description;
    }
}
