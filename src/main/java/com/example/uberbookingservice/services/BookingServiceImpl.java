package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.DriverLocationDto;
import com.example.uberbookingservice.dto.NearbyDriversRequestDto;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberentityservice.models.Booking;
import com.example.uberentityservice.models.BookingStatus;
import com.example.uberentityservice.models.Passenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService{


    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final String locationServiceUrl = "http://localhost:7478/api/location/nearby/drivers";

    public BookingServiceImpl(PassengerRepository passengerRepository, BookingRepository bookingRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        Optional<Passenger> passenger = passengerRepository.findById(bookingDetails.getPassengerId());
       Booking booking = Booking.builder()
               .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
               .startLocation(bookingDetails.getStartLocation())
               .endLocation(bookingDetails.getEndLocation())
//               .passenger(passenger.get())
               .build();
       Booking newBooking = bookingRepository.save(booking);

//       make an api call to location service to get near by driver

        NearbyDriversRequestDto request = NearbyDriversRequestDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();

        ResponseEntity<DriverLocationDto[]> result = restTemplate.postForEntity(
                locationServiceUrl ,request, DriverLocationDto[].class
        );

        assert result.getBody() != null;
        List<DriverLocationDto> driverLocations = Arrays.asList(result.getBody());
        if(result.getStatusCode().is2xxSuccessful() && result.getBody() != null)
        driverLocations.forEach(driverLocation -> {
            System.out.println(driverLocation.getDriverId()+" "+"lat: "+driverLocation.getLatitude()+"long: "+driverLocation.getLongitude()+"");
        });

       return CreateBookingResponseDto.builder()
               .bookingId(newBooking.getId().toString())
               .bookingStatus(newBooking.getBookingStatus().toString())
//               .driver(Optional.of(newBooking.getDriver()))
               .build();
    }
}
