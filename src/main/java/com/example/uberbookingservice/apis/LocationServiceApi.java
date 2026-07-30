package com.example.uberbookingservice.apis;

import com.example.uberbookingservice.dto.DriverLocationDto;
import com.example.uberbookingservice.dto.NearbyDriversRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LocationServiceApi {
    @POST("http://localhost:7478/api/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDrivers(@Body NearbyDriversRequestDto requestDto);
}
