package com.example.myapp.models.response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse <T> {
    private String message;

    @SerializedName("value") // THÊM DÒNG NÀY: Để khớp với image_f66581.png
    private T data;

    public T getData() { return data; }
}
