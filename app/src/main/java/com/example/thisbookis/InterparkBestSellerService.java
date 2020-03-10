package com.example.thisbookis;

import com.example.thisbookis.data.BestSeller;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface InterparkBestSellerService {
    @GET("/api/bestSeller.api")
    Call<BestSeller> getBestSeller(
            @Query("key")String apiKey
            ,@Query("categoryId") String categoryId
            ,@Query("output")String output);
}
