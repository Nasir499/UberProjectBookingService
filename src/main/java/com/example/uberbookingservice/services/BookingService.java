package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberentityservice.models.Booking;

public interface BookingService {
    CreateBookingResponseDto createBooking(CreateBookingDto createBooking);
}
