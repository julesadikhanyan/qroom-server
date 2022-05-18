package com.qroom.user.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qroom.room.api.model.TimeSegmentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HistoryDTO {
    @JsonProperty("id")
    UUID uuid;

    @JsonProperty("roomUuid")
    UUID roomUuid;

    @JsonProperty("time")
    TimeSegmentDTO time;

    @JsonProperty("adminUuid")
    UUID adminUuid;

    @JsonProperty("invitedUsers")
    Map<UUID, String> invitedUsers;

    @JsonProperty("status")
    String status;

    @JsonProperty("title")
    String title;
}
