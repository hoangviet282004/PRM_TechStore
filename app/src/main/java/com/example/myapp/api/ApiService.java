package com.example.myapp.api;

import com.example.myapp.models.request.CreateOrderRequest;
import com.example.myapp.models.request.LoginRequest;
import com.example.myapp.models.request.ManageProductToCartRequest;
import com.example.myapp.models.request.RefreshRequest;
import com.example.myapp.models.request.SignUpRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.CartResponse;
import com.example.myapp.models.response.ChatMessageResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.LoginResponse;
import com.example.myapp.models.response.OrderResponse;
import com.example.myapp.models.response.PageResponse;
import com.example.myapp.models.response.ProductDetailResponse;
import com.example.myapp.models.response.ProductListResponse;
import com.example.myapp.models.response.RefreshResponse;
import com.example.myapp.models.response.SignUpResponse;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    @POST("api/auth/sign-in")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/refresh")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);

    @GET("api/products")
    Call<ApiResponse<PageResponse<ProductListResponse>>> listProducts(
            @Query("brandIds") List<Integer> brandIds,
            @Query("categoryId") Integer categoryId,
            @Query("minPrice") BigDecimal minPrice,
            @Query("maxPrice") BigDecimal maxPrice,
            @Query("keyword") String keyword,
            @Query("sortByPrice") String sortByPrice,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/products/{productId}")
    Call<ApiResponse<ProductDetailResponse>> getProductDetail(@Path("productId") Integer productId);

    @POST("api/cart")
    Call<ApiResponse<CartResponse>> addProduct(@Body ManageProductToCartRequest request);

    @GET("api/cart")
    Call<ApiResponse<CartResponse>> getUserCart();

    @PATCH("api/cart")
    Call<ApiResponse<CartResponse>> adjustQuantity(@Body ManageProductToCartRequest request);

    @DELETE("api/cart/{cartItemId}")
    Call<ApiResponse<CartResponse>> removeItem(@Path("cartItemId") int cartItemId);

    @POST("api/orders")
    Call<ApiResponse<OrderResponse>> createOrder(@Body CreateOrderRequest request);

    @GET("api/payments/{orderId}")
    Call<ApiResponse<String>> getPaymentUrl(@Path("orderId") int orderId);


    // Lấy thông tin phòng chat của khách hàng hiện tại
    @GET("/api/chat/room")
    Call<ApiResponse<ChatRoomResponse>> getMyRoom(@Header("Authorization") String token);

    // Lấy lịch sử tin nhắn trong phòng
    @GET("/api/chat/rooms/{roomId}/messages")
    Call<ApiResponse<PageResponse<ChatMessageResponse>>> getMessages(
            @Header("Authorization") String token,
            @Path("roomId") Integer roomId
    );

    @GET("api/chat/rooms")
    Call<ApiResponse<PageResponse<ChatRoomResponse>>> getAdminRooms(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("size") int size
    );


    @POST("api/chat/rooms/{roomId}/read")
    Call<ApiResponse<Object>> markAsRead(
            @Header("Authorization") String token,
            @Path("roomId") int roomId
    );
}