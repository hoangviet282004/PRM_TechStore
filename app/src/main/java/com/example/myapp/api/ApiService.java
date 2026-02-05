package com.example.myapp.api;

import com.example.myapp.models.request.CreateOrderRequest;
import com.example.myapp.models.request.LoginRequest;
import com.example.myapp.models.request.ManageProductToCartRequest;
import com.example.myapp.models.request.RefreshRequest;
import com.example.myapp.models.request.SignUpRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.CartResponse;
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
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    // Theo image_f82f1e.png, đường dẫn là api/auth/sign-in
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
            @Query("keyword") String keyword,      // Khớp với tham số keyword của BE
            @Query("sortByPrice") String sortByPrice, // Khớp với defaultValue = "asc"
            @Query("page") int page,
            @Query("size") int size
    );

    // Thêm vào interface hiện tại của bạn
    @GET("api/products/{productId}")
    Call<ApiResponse<ProductDetailResponse>> getProductDetail(@Path("productId") Integer productId);

    // ======================== Cart ==============================
    @POST("api/cart") // Thêm sản phẩm
    Call<ApiResponse<CartResponse>> addProduct(@Body ManageProductToCartRequest request);

    @GET("api/cart") // Lấy giỏ hàng
    Call<ApiResponse<CartResponse>> getUserCart();

    @PATCH("api/cart") // Sửa số lượng
    Call<ApiResponse<CartResponse>> adjustQuantity(@Body ManageProductToCartRequest request);

    @DELETE("api/cart/{cartItemId}") // Xóa 1 món
    Call<ApiResponse<CartResponse>> removeItem(@Path("cartItemId") int cartItemId);

    // Luồng: Cart -> Order -> Payment
    @POST("api/orders")
    Call<ApiResponse<OrderResponse>> createOrder(@Body CreateOrderRequest request);

    @GET("api/payments/{orderId}")
    Call<ApiResponse<String>> getPaymentUrl(@Path("orderId") int orderId);

}
