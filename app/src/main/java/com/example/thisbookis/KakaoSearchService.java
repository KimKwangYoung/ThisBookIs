package com.example.thisbookis;

import com.example.thisbookis.data.SearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface KakaoSearchService {

    @GET("/v3/search/book")
    Call<SearchResult> getBookList(
            @Header("Authorization") String restApiKey
            ,@Query("query")String searchKeyword
            ,@Query("size") Integer size
            ,@Query("page") Integer page);

}
