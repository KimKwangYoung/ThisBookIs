package com.example.thisbookis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InterParkApiClient {
    private Retrofit retrofit;
    public InterparkBestSellerService bestSellerService;
    public static InterParkApiClient interparkApiClient = new InterParkApiClient();
    public static final String baseUrl = "http://book.interpark.com";


    public InterParkApiClient(){
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        bestSellerService = retrofit.create(InterparkBestSellerService.class);
    }

    public static InterParkApiClient getInstance(){
        return interparkApiClient;
    }
}
