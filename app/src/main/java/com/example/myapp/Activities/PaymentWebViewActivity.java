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
                if ("techexpress".equals(uri.getScheme())) {
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
        String status = uri.getQueryParameter("status");
        if ("PAID".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
            showSuccessDialog();
        } else {
            Toast.makeText(this, "Thanh toán thất bại hoặc bị huỷ.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showSuccessDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_payment_success);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btnGoHome).setOnClickListener(v -> {
            dialog.dismiss();
            goHome();
        });
        dialog.show();
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
