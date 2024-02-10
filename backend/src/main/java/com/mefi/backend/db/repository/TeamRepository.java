package com.mefi.backend.db.repository;

import com.mefi.backend.db.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA 기반 TeamRepository 공통 인터페이스
public interface TeamRepository extends JpaRepository<Team, Long> {
}
