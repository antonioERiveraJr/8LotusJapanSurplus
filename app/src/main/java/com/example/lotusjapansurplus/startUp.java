package com.example.lotusjapansurplus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

public class startUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
        showImagePopup();
    }
    private void showImagePopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.image_popup_layout);

        ImageView imageView = dialog.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.logo); // Replace 'lotus_final' with your actual image drawable

        dialog.show();

        // Dismiss the dialog after 2 seconds
        new Handler().postDelayed(dialog::dismiss, 2000);
        Intent intent = new Intent(startUp.this,MainActivity.class);
        startActivity(intent);
    }

}