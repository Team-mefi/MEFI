package com.mefi.backend.db.repository;

import com.mefi.backend.db.entity.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Spring Data JPA 기반 TeamUserRepository 공통 인터페이스 + 사용자 정의 인터페이스 상속
public interface TeamUserRepository extends JpaRepository<UserTeam, Long>, TeamUserRepositoryCustom {

    // 팀원 조회
    @Query("SELECT ut FROM UserTeam ut WHERE ut.user.id = :userId AND ut.team.id = :teamId")
    Optional<UserTeam> findMember(@Param("userId") Long userId,@Param("teamId") Long teamId);

    // 팀원 삭제
    @Modifying
    @Query("DELETE FROM UserTeam ut WHERE ut.user.id = :userId AND ut.team.id = :teamId")
    void deleteMember(Long userId, Long teamId);

    // 팀원 PK 조회
    @Query("SELECT ut.user.id FROM UserTeam ut WHERE  ut.team.id = :teamId")
    List<Long> findByTeamId(@Param("teamId") Long teamId);
}
