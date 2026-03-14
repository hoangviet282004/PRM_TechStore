package com.example.myapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry point for the deep link techexpress://payment_result
 * Reads the 'status' query param from PayOS and routes to the
 * appropriate result screen, then finishes itself.
 */
public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routeFromDeepLink(getIntent().getData());
    }

    private void routeFromDeepLink(Uri data) {
        String orderCode = null;
        boolean isSuccess = false;

        if (data != null) {
            String status = data.getQueryParameter("status");
            orderCode = data.getQueryParameter("orderCode");
            isSuccess = "PAID".equalsIgnoreCase(status) || "Processing".equalsIgnoreCase(status);
        }

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
}
