package com.example.lotusjapansurplus.buyer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class BuyerEditDetailsActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText methodEditText;
    private EditText addressEditText;
    private EditText balanceEditText;
    private Spinner statusEditText;
    private EditText contactEditText;

    private EditText noteEditText;
    private String fb;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_edit_details);
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When the "Back" button is clicked, navigate back to the previous activity (e.g., MainActivity)
                Intent intent = new Intent(BuyerEditDetailsActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        noteEditText = findViewById(R.id.editNoteEditText);
        nameEditText = findViewById(R.id.editNameEditText);
        methodEditText = findViewById(R.id.editMethodEditText);
        addressEditText = findViewById(R.id.editAddressEditText);
        balanceEditText = findViewById(R.id.editBalanceEditText);
        statusEditText = findViewById(R.id.statusSpinner);
        contactEditText = findViewById(R.id.editContactEditText);

        String selectedStatus = statusEditText.getSelectedItem().toString();
        // Retrieve data passed through Intent
        Intent intent = getIntent();
        if (intent != null) {
            fb = intent.getStringExtra("fb");
            String name = intent.getStringExtra("name");
            String method = intent.getStringExtra("method");
            String address = intent.getStringExtra("address");
            double balance = intent.getDoubleExtra("balance", 0.0);
            String status = intent.getStringExtra("status");
            String contact = intent.getStringExtra("contact");
            String note = intent.getStringExtra("note");

            // Populate EditText fields with the data
            nameEditText.setText(name);
            methodEditText.setText(method);
            addressEditText.setText(address);
            balanceEditText.setText(String.valueOf(balance));
            statusEditText.setTag(status);
            contactEditText.setText(contact);
            noteEditText.setText(note);
        }

        // Save Button
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the details in the database and return to BuyerDetailsActivity
                showConfirmationDialog();
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Update");
        builder.setMessage(
                "Name: " + nameEditText.getText().toString() + "\n" +
                        "Method: " + methodEditText.getText().toString() + "\n" +
                        "Address: " + addressEditText.getText().toString() + "\n" +
                        "Balance: " + balanceEditText.getText().toString() + "\n" +
                        "Status: " + statusEditText.getSelectedItem().toString() + "\n" +
                        "Contact: " + contactEditText.getText().toString() + "\n\n" +
                        "Note: " + noteEditText.getText().toString() +
                        "Do you want to save these changes?"
        );

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateBuyerDetails();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, return to editing
            }
        });

        builder.show();
    }

    private void updateBuyerDetails() {
        // Update details in the database using the provided "fb" key
        // Use the data from the EditText fields to update the buyer's details
        String updatedName = nameEditText.getText().toString();
        String updatedMethod = methodEditText.getText().toString();
        String updatedAddress = addressEditText.getText().toString();
        double updatedBalance = Double.parseDouble(balanceEditText.getText().toString());
        String updatedStatus = statusEditText.getSelectedItem().toString();
        String updatedContact = contactEditText.getText().toString();
        String updateNote = noteEditText.getText().toString();
        // Implement the code to update the database with the new details using Firebase, assuming you have a Firebase reference
        // Firebase reference example:

        String id = getIntent().getStringExtra("id");

        progressDialog.show();

        DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer").child(id);

        // Creating the buyerMap to update the buyer's details
        HashMap<String, Object> buyerMap = new HashMap<>();
        buyerMap.put("name", updatedName);
        buyerMap.put("method", updatedMethod);
        buyerMap.put("address", updatedAddress);
        buyerMap.put("balance", updatedBalance);
        buyerMap.put("status", updatedStatus);
        buyerMap.put("contact", updatedContact);
        buyerMap.put("note", updateNote);

        buyerRef.updateChildren(buyerMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Intent backIntent = new Intent(BuyerEditDetailsActivity.this, BuyerActivity.class);
                        backIntent.putExtra("fb", fb);
                        startActivity(backIntent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        // Handle the error case
                    }
                });
    }
}

