package com.example.myapp.api;

import com.example.myapp.models.request.CreateOrderRequest;
import com.example.myapp.models.request.LoginRequest;
import com.example.myapp.models.request.ManageProductToCartRequest;
import com.example.myapp.models.request.RefreshRequest;
import com.example.myapp.models.request.SignUpRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.BrandResponse;
import com.example.myapp.models.response.CartResponse;
import com.example.myapp.models.response.CategoryResponse;
import com.example.myapp.models.response.ChatMessageResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.LoginResponse;
import com.example.myapp.models.response.OrderResponse;
import com.example.myapp.models.response.OrderSummaryResponse;
import com.example.myapp.models.response.PaymentResponse;
import com.example.myapp.models.response.PageResponse;
import com.example.myapp.models.response.ProductDetailResponse;
import com.example.myapp.models.response.ProductListResponse;
import com.example.myapp.models.response.RefreshResponse;
import com.example.myapp.models.response.SignUpResponse;
import com.example.myapp.models.response.UserResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // --- USER ---
    @GET("api/users/me")
    Call<ApiResponse<UserResponse>> getProfile();

    // --- AUTH ---
    @POST("api/auth/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    @POST("api/auth/sign-in")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/refresh")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);

    // --- PRODUCTS ---
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

    // --- CART ---
    @POST("api/cart")
    Call<ApiResponse<CartResponse>> addProduct(@Body ManageProductToCartRequest request);

    @GET("api/cart")
    Call<ApiResponse<CartResponse>> getUserCart();

    @PATCH("api/cart")
    Call<ApiResponse<CartResponse>> adjustQuantity(@Body ManageProductToCartRequest request);

    @DELETE("api/cart/{cartItemId}")
    Call<ApiResponse<CartResponse>> removeItem(@Path("cartItemId") int cartItemId);

    // --- ORDERS & PAYMENTS ---
    @POST("api/orders")
    Call<ApiResponse<OrderResponse>> createOrder(@Body CreateOrderRequest request);

    @GET("api/payments/summary/{orderCode}")
    Call<ApiResponse<OrderSummaryResponse>> getOrderSummary(@Path("orderCode") String orderCode);

    @GET("api/payments/{orderId}")
    Call<ApiResponse<String>> getPaymentUrl(@Path("orderId") int orderId);

    // --- CHAT SYSTEM ---
    // Authorization is handled automatically by the OkHttp interceptor in RetrofitClient

    @GET("api/chat/users/{username}/presence")
    Call<ApiResponse<Map<String, Object>>> getUserPresence(@Path("username") String username);

    @GET("api/chat/room")
    Call<ApiResponse<ChatRoomResponse>> getMyRoom();

    @GET("api/chat/rooms")
    Call<ApiResponse<PageResponse<ChatRoomResponse>>> getAdminRooms(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/chat/rooms/search")
    Call<ApiResponse<PageResponse<ChatRoomResponse>>> searchRooms(
            @Query("q") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/chat/rooms/{roomId}/messages")
    Call<ApiResponse<PageResponse<ChatMessageResponse>>> getMessages(
            @Path("roomId") Integer roomId,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("api/chat/rooms/{roomId}/read")
    Call<ApiResponse<Map<String, Integer>>> markAsRead(@Path("roomId") int roomId);

    @GET("api/chat/rooms/{roomId}/unread-count")
    Call<ApiResponse<Map<String, Long>>> getUnreadCount(@Path("roomId") int roomId);

    @GET("api/chat/messages/{messageId}/status")
    Call<ApiResponse<Map<String, Object>>> getMessageReadStatus(@Path("messageId") Integer messageId);

    @GET("api/brands")
    Call<ApiResponse<List<BrandResponse>>> getAllBrands();

    @GET("api/categories")
    Call<ApiResponse<List<CategoryResponse>>> getAllCategories();
}
