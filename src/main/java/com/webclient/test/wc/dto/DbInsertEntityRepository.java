package com.webclient.test.wc.dto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DbInsertEntityRepository extends JpaRepository<DbInsertEntity, Long> {

	Optional<DbInsertEntity> findByCountAndRequestTime(Integer count, String time);
}
