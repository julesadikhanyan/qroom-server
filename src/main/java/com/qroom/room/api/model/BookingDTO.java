package com.qroom.room.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BookingDTO {
    @JsonProperty("title")
    @NotBlank(message = "Cat`n create meeting without title")
    String title;

    @JsonProperty("roomUuid")
    @NotNull(message = "Cat`n create meeting without specified room")
    UUID roomUuid;

    @JsonProperty("time")
    @NotNull(message = "Cat`n create meeting without specified time")
    TimeSegmentDTO time;

    @JsonProperty("invitedUsers")
    @NotNull(message = "Cat`n create meeting with null invited")
    List<UUID> invitedUsers;
}
