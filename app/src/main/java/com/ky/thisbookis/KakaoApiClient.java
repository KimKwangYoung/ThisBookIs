package com.ky.thisbookis;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KakaoApiClient {

    private Retrofit retrofit;
    public KakaoSearchService searchService;
    public static KakaoApiClient kakaoApiClient = new KakaoApiClient();
    public static final String baseUrl = "https://dapi.kakao.com";

    public KakaoApiClient(){
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        searchService = retrofit.create(KakaoSearchService.class);
    }

    public static KakaoApiClient getInstance(){
        return kakaoApiClient;
    }
}
