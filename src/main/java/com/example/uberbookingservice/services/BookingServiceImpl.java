package com.example.uberbookingservice.services;

import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.DriverLocationDto;
import com.example.uberbookingservice.dto.NearbyDriversRequestDto;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberentityservice.models.Booking;
import com.example.uberentityservice.models.BookingStatus;
import com.example.uberentityservice.models.Passenger;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService{


    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;

    private final LocationServiceApi locationServiceApi;
//    private final String locationServiceUrl = "http://localhost:7478/api/location/nearby/drivers";

    public BookingServiceImpl(PassengerRepository passengerRepository, BookingRepository bookingRepository, LocationServiceApi locationServiceApi) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        RestTemplate restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
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

        processNearbyDriversAsync(request);
//
//        ResponseEntity<DriverLocationDto[]> result = restTemplate.postForEntity(
//                locationServiceUrl ,request, DriverLocationDto[].class
//        );
//
//        assert result.getBody() != null;
//        List<DriverLocationDto> driverLocations = Arrays.asList(result.getBody());
//        if(result.getStatusCode().is2xxSuccessful() && result.getBody() != null)
//        driverLocations.forEach(driverLocation -> {
//            System.out.println(driverLocation.getDriverId()+" "+"lat: "+driverLocation.getLatitude()+"long: "+driverLocation.getLongitude()+"");
//        });

       return CreateBookingResponseDto.builder()
               .bookingId(newBooking.getId().toString())
               .bookingStatus(newBooking.getBookingStatus().toString())
//               .driver(Optional.of(newBooking.getDriver()))
               .build();

    }

    private void processNearbyDriversAsync(NearbyDriversRequestDto request){
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(request);

        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(@NonNull Call<DriverLocationDto[]> call, @NonNull Response<DriverLocationDto[]> response) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(response.isSuccessful() && response.body()!=null){
                   List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                   driverLocations.forEach(driverLocation -> {
                       System.out.println(driverLocation.getDriverId()+" "+"lat: "+driverLocation.getLatitude()+"long: "+driverLocation.getLongitude()+"");
                   });
               }else {
                   System.out.println("error");
               }
            }

            @Override
            public void onFailure(@NonNull Call<DriverLocationDto[]> call, @NonNull Throwable t) {
                t.getStackTrace();
            }
        });
    }
}
