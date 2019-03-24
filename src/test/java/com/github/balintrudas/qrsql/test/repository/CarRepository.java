package com.github.balintrudas.qrsql.test.repository;

import com.github.balintrudas.qrsql.test.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}