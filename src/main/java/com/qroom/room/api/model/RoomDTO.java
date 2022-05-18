package com.qroom.room.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RoomDTO {
    @JsonProperty("id")
    private UUID uuid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("photoUrl")
    private String photoUrl;

    @JsonProperty("numberOfSeats")
    private Integer numberOfSeats;

    @JsonProperty("floor")
    private Integer floor;

    @JsonProperty("isFree")
    private Boolean isFree;
}
