package com.qroom.room.impl.entity;

import com.qroom.user.impl.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@IdClass(HistoryUserId.class)
public class HistoryUser {
    @Id
    @Column(name = "history_uuid")
    private UUID historyUuid;
    @Id
    @Column(name = "user_uuid")
    private UUID userUuid;
    private Boolean isAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    HistoryUserStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "history_uuid",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private History history;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_uuid",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private User user;
}
