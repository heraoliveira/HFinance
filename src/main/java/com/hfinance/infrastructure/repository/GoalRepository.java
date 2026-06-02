package com.hfinance.infrastructure.repository;

import com.hfinance.domain.model.Goal;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {
    Goal save(Goal goal);
    void update(Goal goal);
    void delete(Long id);
    Optional<Goal> findById(Long id);
    List<Goal> findAll();
}
