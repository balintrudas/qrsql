package com.github.balintrudas.qrsql.test.repository;

import com.github.balintrudas.qrsql.test.model.Screw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrewRepository extends JpaRepository<Screw, Long> {
}