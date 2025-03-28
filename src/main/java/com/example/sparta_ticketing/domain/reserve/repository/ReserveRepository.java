package com.example.sparta_ticketing.domain.reserve.repository;

import com.example.sparta_ticketing.domain.reserve.entity.Reserve;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReserveRepository extends JpaRepository<Reserve, Long> {
}
