package com.example.myapp.models.response;

import java.util.List;

public class PageResponse <T> {
    private List<T> content;
    private int pageNumber;
    private int totalPages;
    // Getters...
    public List<T> getContent() { return content; }
}
