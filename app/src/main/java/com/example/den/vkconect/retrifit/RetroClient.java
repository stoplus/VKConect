package com.example.den.vkconect.retrifit;

import com.example.den.vkconect.retrifit.ApiVKRequest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by den on 07.02.2018.
 */

public class RetroClient {
    private static final String ROOT_URL = "http://api.now-android.ru";

    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiVKRequest getMyApi() {
        return getRetrofitInstance().create(ApiVKRequest.class);
    }
}
