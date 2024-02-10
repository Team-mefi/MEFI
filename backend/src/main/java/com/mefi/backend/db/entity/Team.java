package com.mefi.backend.db.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor
public class Team {

    // 식별ID
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임하는 전략
    private Long id;
    
    // 팀명
    private String name;

    // 팀소개
    private String description;

    // 생성시간
    private LocalDateTime createdTime;

    /*
     * 사용자 팀 엔티티 목록 (사용자-팀 연결테이블 매핑)
     * CascadeType.ALL을 통해 해당 팀이 영구적으로 삭제되면 관련된 UserTeam도 함께 삭제됩니다.
     */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<UserTeam> userTeams = new ArrayList<>();

    /*
     * 회의 엔티티 목록
     * 하나의 팀은 여러 회의와 관련될 수 있다.
     */
    @OneToMany(mappedBy = "team")
    private List<Conference> conferences = new ArrayList<>();

    // 새로운 팀 객체를 생성하는 빌더
    @Builder
    public Team(String name, String description, LocalDateTime createdTime){
        this.name = name;
        this.description = description;
        this.createdTime = createdTime;
    }

    // 팀 멤버 추가 메서드
    public void addUserTeam(UserTeam member) {
        this.userTeams.add(member);
    }

    // 팀 정보 수정 메서드
    public void modifyInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
