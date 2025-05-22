package com.example.hrsystem.repository;

import com.example.hrsystem.model.ActiveRequestsCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ActiveRequestsCounterRepository extends JpaRepository<ActiveRequestsCounter, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE ActiveRequestsCounter arc SET arc.counter = arc.counter + 1 WHERE arc.id = 1")
    void incrementCounter();

    @Transactional
    @Modifying
    @Query("UPDATE ActiveRequestsCounter arc SET arc.counter = 0 WHERE arc.id = 1")
    void resetCounter();
}