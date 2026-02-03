package com.example.myapp.models.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductDetailResponse {
    private Integer id;
    private String productName;
    private String fullDescription;
    private Map<String, Object> technicalSpecifications;
    private BigDecimal price; // Sử dụng BigDecimal cho độ chính xác cao
    private String primaryImageUrl;
    private List<String> additionalImageUrls; // Cho yêu cầu Multiple Images

    // Getters
    public Integer getId() { return id; }
    public String getProductName() { return productName; }
    public String getFullDescription() { return fullDescription; }
    public Map<String, Object> getTechnicalSpecifications() { return technicalSpecifications; }
    public BigDecimal getPrice() { return price; }
    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public List<String> getAdditionalImageUrls() { return additionalImageUrls; }
}