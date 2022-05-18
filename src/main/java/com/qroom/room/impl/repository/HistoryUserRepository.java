package com.qroom.room.impl.repository;

import com.qroom.room.impl.entity.HistoryUser;
import com.qroom.room.impl.entity.HistoryUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryUserRepository extends JpaRepository<HistoryUser, HistoryUserId> {
}
