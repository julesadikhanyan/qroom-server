package com.qroom.room.impl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue
    private UUID uuid;
    private String title;
    //@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime startTime;
    //@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private HistoryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_uuid")
    private Room room;

    @OneToMany(mappedBy = "history", fetch = FetchType.LAZY)
    List<HistoryUser> historyUsers;
}
