package com.qroom.room.impl.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Room {

    @Id
    @GeneratedValue
    private UUID uuid;
    private String name;
    private String photoUrl;
    private Integer numberOfSeats;
    private Integer floor;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<History> histories;
}
