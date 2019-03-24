package com.github.balintrudas.qrsql.test.repository;

import com.github.balintrudas.qrsql.test.model.Engine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngingeRepository extends JpaRepository<Engine, Long> {
}