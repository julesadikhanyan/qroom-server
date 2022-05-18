package com.qroom.room.impl.repository;

import com.qroom.room.impl.entity.HistoryUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryUserStatusRepository extends JpaRepository<HistoryUserStatus, Integer> {
    HistoryUserStatus findHistoryUserStatusByName(String name);
    enum StatusNames {
        PENDING, CONFIRMED, REJECTED
    }
}
