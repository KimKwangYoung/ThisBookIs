package com.example.thisbookis;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private Retrofit retrofit;
    public KakaoSearchService searchService;
    public static ApiClient apiClient = new ApiClient();
    public static final String baseUrl = "https://dapi.kakao.com";

    public ApiClient(){
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        searchService = retrofit.create(KakaoSearchService.class);
    }

    public static ApiClient getInstance(){
        return apiClient;
    }
}
