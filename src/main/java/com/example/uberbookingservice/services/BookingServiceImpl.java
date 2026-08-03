package com.example.uberbookingservice.services;
import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.apis.UberSocketApi;
import com.example.uberbookingservice.dto.*;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.DriverRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberentityservice.models.Booking;
import com.example.uberentityservice.models.BookingStatus;
import com.example.uberentityservice.models.Driver;
import com.example.uberentityservice.models.Passenger;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService{


    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;

    private final UberSocketApi uberSocketApi;

    private final LocationServiceApi locationServiceApi;
    private final DriverRepository driverRepository;
//    private final String locationServiceUrl = "http://localhost:7478/api/location/nearby/drivers";

    public BookingServiceImpl(PassengerRepository passengerRepository, BookingRepository bookingRepository, UberSocketApi uberSocketApi, LocationServiceApi locationServiceApi, DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.uberSocketApi = uberSocketApi;
        RestTemplate restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
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

        processNearbyDriversAsync(request,bookingDetails.getPassengerId(),newBooking.getId());
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

    @Override
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId) {
        Optional<Driver> driver = driverRepository.findById(bookingRequestDto.getDriverId().get());
        if(driver.isPresent() && driver.get().getIsAvailable()){
            BookingStatus status = BookingStatus.valueOf(bookingRequestDto.getStatus());
            bookingRepository.updateBookingStatusAndDriverById(bookingId,status,driver.get());
            return UpdateBookingResponseDto.builder()
                    .bookingId(bookingId)
                    .status(bookingRequestDto.getStatus())
                    .driver(driver)
                    .build();
        }else{
            return null;
        }
    }

    private void processNearbyDriversAsync(NearbyDriversRequestDto request,Long passengerId,Long bookingId){
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(request);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(@NonNull Call<DriverLocationDto[]> call, @NonNull Response<DriverLocationDto[]> response) {
                if(response.isSuccessful() && response.body()!=null){
                   List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                   driverLocations.forEach(driverLocation -> {
                       System.out.println(driverLocation.getDriverId()+" "+"lat: "+driverLocation.getLatitude()+"long: "+driverLocation.getLongitude()+"");
                   });
                    raiseRideRequestAsync(RideRequestDto.builder().passengerId(passengerId).bookingId(bookingId).build());
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

    private void raiseRideRequestAsync(RideRequestDto request){
        Call<Boolean> call = uberSocketApi.raiseRideRequest(request);

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean result = response.body();
                    System.out.println("ride request raised: " + result.toString());
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                t.getStackTrace();
            }
        });
    }
}

