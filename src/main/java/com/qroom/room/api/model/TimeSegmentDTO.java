package com.qroom.room.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TimeSegmentDTO {
    @JsonProperty("start")
    private OffsetDateTime startTime;

    @JsonProperty("end")
    private OffsetDateTime endTime;
}
