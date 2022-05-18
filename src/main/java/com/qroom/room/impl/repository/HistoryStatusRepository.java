package com.qroom.room.impl.repository;

import com.qroom.room.impl.entity.HistoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryStatusRepository extends JpaRepository<HistoryStatus, Integer> {
    HistoryStatus findByName(String name);
    enum StatusNames {
        BOOKED, CANCELED, PASSED
    }
}
