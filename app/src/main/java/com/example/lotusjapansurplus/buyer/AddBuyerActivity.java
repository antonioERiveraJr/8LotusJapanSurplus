package com.example.lotusjapansurplus.buyer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotusjapansurplus.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class AddBuyerActivity extends AppCompatActivity {

    private EditText fbEditText, nameEditText, addressEditText, contactEditText, notesEditText;
    private Spinner methodSpinner;
    private DatabaseReference databaseReference;


    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_buyer);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Buyer");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        notesEditText = findViewById(R.id.notesEditText);
        fbEditText = findViewById(R.id.fbEditText);
        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        contactEditText = findViewById(R.id.contactEditText);
        methodSpinner = findViewById(R.id.methodSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.method_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        methodSpinner.setAdapter(adapter);

        Button addBuyerButton = findViewById(R.id.addBuyerButton);
        addBuyerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBuyerToFirebase();
            }
        });
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(AddBuyerActivity.this, BuyerActivity.class);
                startActivity(intent1);
            }
        });
    }

    public void addBuyerToFirebase() {
        String fb = fbEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String method = methodSpinner.getSelectedItem().toString();
        String address = addressEditText.getText().toString();
        String contact = contactEditText.getText().toString();
        String notes = notesEditText.getText().toString();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
        String dateTime = dateFormat.format(calendar.getTime());
        String productRandomKey = dateTime;

        HashMap<String, Object> buyerMap = new HashMap<>();
        buyerMap.put("fb", fb);
        buyerMap.put("name", name);
        buyerMap.put("method", method);
        buyerMap.put("address", address);
        buyerMap.put("balance", 0.0);
        buyerMap.put("note",notes);
        buyerMap.put("status", "unidentified");
        buyerMap.put("id", productRandomKey);
        buyerMap.put("contact", contact);
        progressDialog.show();

        databaseReference.child(productRandomKey).updateChildren(buyerMap
                , (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        progressDialog.dismiss();
                        // Handle the error case
                        Toast.makeText(AddBuyerActivity.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        navigateToMainActivity();
                    }
                });

        navigateToMainActivity();
    }

    public void navigateToMainActivity() {
        Intent intent = new Intent(AddBuyerActivity.this, BuyerActivity.class);
        startActivity(intent);
    }
}