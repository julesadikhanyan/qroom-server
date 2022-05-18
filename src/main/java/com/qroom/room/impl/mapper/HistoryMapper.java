package com.qroom.room.impl.mapper;

import com.qroom.user.api.model.HistoryDTO;
import com.qroom.room.api.model.TimeSegmentDTO;
import com.qroom.room.impl.entity.History;
import com.qroom.room.impl.entity.HistoryUser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class HistoryMapper {
    public static TimeSegmentDTO historyToTimeSegmentDTO(History history){
        return new TimeSegmentDTO(
                history.getStartTime(),
                history.getEndTime()
        );
    }

    public static HistoryDTO historyToHistoryDTO(History history){
        Map<UUID, String> invitedUsers = new HashMap<>();
        history.getHistoryUsers()
                .stream()
                .filter(historyUser -> (!historyUser.getIsAdmin()))
                .forEach(historyUser ->
                        invitedUsers.put(historyUser.getUserUuid(), historyUser.getStatus().getName())
                );
        return new HistoryDTO(
                history.getUuid(),
                history.getRoom().getUuid(),
                new TimeSegmentDTO(history.getStartTime(), history.getEndTime()),
                history.getHistoryUsers()
                        .stream()
                        .filter(HistoryUser::getIsAdmin)
                        .collect(Collectors.toList())
                        .get(0).getUserUuid(),
                invitedUsers,
                history.getStatus().getName(),
                history.getTitle()
        );
    }
}
