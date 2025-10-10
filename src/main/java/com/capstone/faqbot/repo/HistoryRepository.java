package com.capstone.faqbot.repo;

import com.capstone.faqbot.model.HistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryResponse, Long> {
}
