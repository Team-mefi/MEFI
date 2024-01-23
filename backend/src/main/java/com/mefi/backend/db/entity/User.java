package com.mefi.backend.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 유저 모델 정의
 */
@Entity
@Getter
public class User {
    // 회원식별ID
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임하는 전략
    private Long id;

    // 아이디(회원 이메일)
    private String email;

    // 비밀번호
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // 이름
    private String name;

    // 부서
    private String dept;

    // 직책
    private String position;

    // 생성시간
    private LocalDateTime createdTime;

    // 프로필사진경로
    private String imgUrl;

    // 리프레시 토큰
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id")
    private Token token;

    // 이메일 인증
    @OneToOne(fetch = FetchType.LAZY)
    private EmailAuth emailAuth;

    // 개인일정
    @OneToMany(mappedBy = "user")
    private List<PrivateSchedule> schedules = new ArrayList<>();

    // 알림
    @OneToMany(mappedBy = "user")
    private List<Noti> notis = new ArrayList<>();

    // 팀과 연결 테이블
    @OneToMany(mappedBy = "user")
    private List<UserTeam> userTeams = new ArrayList<>();

    // 회의와 연결 테이블
    @OneToMany(mappedBy = "user")
    private List<UserConference> userConferences = new ArrayList<>();
}