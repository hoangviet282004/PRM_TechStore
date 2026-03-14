package com.example.myapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.R;
import com.example.myapp.databinding.ActivityPaymentWebviewBinding;

public class PaymentWebViewActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "PAYMENT_URL";

    private ActivityPaymentWebviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarPayment);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL thanh toán không hợp lệ.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(String url) {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                // Intercept the PayOS server callback before the WebView tries to connect.
                // Covers both localhost (emulator) and 10.0.2.2, and the techexpress:// deep link.
                String path = uri.getPath() != null ? uri.getPath() : "";
                if (path.contains("payos-cancel")) {
                    navigateToResult(false, null);
                    return true;
                }
                if (path.contains("payos-return") || "techexpress".equals(uri.getScheme())) {
                    handlePaymentResult(uri);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.loadingOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.loadingOverlay.setVisibility(View.GONE);
            }
        });

        binding.webView.loadUrl(url);
    }

    private void handlePaymentResult(Uri uri) {
        String code = uri.getQueryParameter("code");       // PayOS: "00" = success
        String status = uri.getQueryParameter("status");   // deep link fallback: "PAID"
        String orderCode = uri.getQueryParameter("orderCode");
        boolean isSuccess = "00".equals(code)
                || "PAID".equalsIgnoreCase(status)
                || "success".equalsIgnoreCase(status);
        navigateToResult(isSuccess, orderCode);
    }

    private void navigateToResult(boolean isSuccess, String orderCode) {
        Intent intent;
        if (isSuccess) {
            intent = new Intent(this, PaymentSuccessActivity.class);
            intent.putExtra(PaymentSuccessActivity.EXTRA_ORDER_CODE, orderCode);
        } else {
            intent = new Intent(this, PaymentFailedActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            finish();
        }
        return true;
    }
}
