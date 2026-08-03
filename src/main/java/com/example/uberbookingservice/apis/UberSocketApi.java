package com.example.uberbookingservice.apis;

import com.example.uberbookingservice.dto.DriverLocationDto;
import com.example.uberbookingservice.dto.NearbyDriversRequestDto;
import com.example.uberbookingservice.dto.RideRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UberSocketApi {
    @POST("http://localhost:7476/api/socket/newride")
    Call<Boolean> raiseRideRequest(@Body RideRequestDto requestDto) ;
}
