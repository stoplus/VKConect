package com.example.den.vkconect;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Status {

    @SerializedName("response")
    @Expose
    private int response;

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

}