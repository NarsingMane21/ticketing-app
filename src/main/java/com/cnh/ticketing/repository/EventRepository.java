package com.cnh.ticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnh.ticketing.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}