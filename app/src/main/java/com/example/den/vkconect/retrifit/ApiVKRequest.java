package com.example.den.vkconect.retrifit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by den on 07.02.2018.
 */

public interface ApiVKRequest {
    @Multipart
    @POST
    Call<ResponseBody> sendFoto( @Part("description") RequestBody description,
                                 @Part MultipartBody.Part file);
}//ApiVKRequest
