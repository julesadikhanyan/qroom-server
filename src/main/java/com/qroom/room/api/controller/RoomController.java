package com.qroom.room.api.controller;

import com.qroom.common.security.UserAuth;
import com.qroom.room.api.model.BookingDTO;
import com.qroom.user.api.model.HistoryDTO;
import com.qroom.room.api.model.RoomDTO;
import com.qroom.room.api.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping()
    ResponseEntity<List<RoomDTO>> getAllRooms() {
        return new ResponseEntity<>(service.getAllRooms(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<RoomDTO> getRoom(
            @PathVariable(name = "id") @NotNull UUID id
    ) {
        return new ResponseEntity<>(service.getRoom(id), HttpStatus.OK);
    }

    @GetMapping("/booking")
    ResponseEntity<List<HistoryDTO>> getRoomBookings(
            @RequestParam(name = "room_uuid") UUID roomUuid,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "dd.MM.yyyy") Date date
    ) {
        return new ResponseEntity<>(service.getRoomBookings(roomUuid, date), HttpStatus.OK);
    }

    @GetMapping("/booking/{booking_id}")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    ResponseEntity<HistoryDTO> getRoomBooking(
            Authentication authentication,
            @PathVariable(name = "booking_id") UUID bookingUuid
    ) {
        return new ResponseEntity<>(service.getRoomBooking(bookingUuid), HttpStatus.OK);
    }

    @PostMapping("/booking")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    @Valid
    ResponseEntity<String> bookRoom(
            Authentication authentication,
            @RequestBody @Valid BookingDTO bookingDTO
    ) {
        UserAuth userAuth = (UserAuth) authentication.getPrincipal();
        service.bookRoom(bookingDTO, userAuth.getUuid());
        return new ResponseEntity<>("Room booked successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/booking/{booking_id}")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    ResponseEntity<String> deleteRoomBooking(
            Authentication authentication,
            @PathVariable(name = "booking_id") UUID bookingUuid
    ) {
        UserAuth userAuth = (UserAuth) authentication.getPrincipal();
        service.deleteRoomBooking(bookingUuid, userAuth.getUuid());
        return new ResponseEntity<>("Successfully delete booking", HttpStatus.NO_CONTENT);
    }

    @PutMapping("/booking/{booking_id}")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    ResponseEntity<String> confirmAccess(
            Authentication authentication,
            @PathVariable(name = "booking_id") UUID bookingUuid
    ) {
        UserAuth userAuth = (UserAuth) authentication.getPrincipal();
        service.confirmBooking(bookingUuid, userAuth.getUuid());
        return new ResponseEntity<>("Access confirmed", HttpStatus.OK);
    }
}