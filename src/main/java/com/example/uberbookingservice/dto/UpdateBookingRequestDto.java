package com.example.uberbookingservice.dto;

import com.example.uberentityservice.models.BookingStatus;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequestDto {
    private String status;
    private Optional<Long> driverId;
}
