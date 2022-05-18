package com.qroom.room.impl.mapper;

import com.qroom.room.api.model.RoomDTO;
import com.qroom.room.impl.entity.Room;

public class RoomMapper {
    public static RoomDTO roomToDTO(Room room, Boolean isFree) {
        return new RoomDTO(
                room.getUuid(),
                room.getName(),
                room.getPhotoUrl(),
                room.getNumberOfSeats(),
                room.getFloor(),
                isFree
        );
    }
}
