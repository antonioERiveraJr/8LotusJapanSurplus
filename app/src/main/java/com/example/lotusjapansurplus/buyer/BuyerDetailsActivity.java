package com.example.lotusjapansurplus.buyer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.lotusjapansurplus.R;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BuyerDetailsActivity extends AppCompatActivity {

    private TextView fbTextView;
    private TextView nameTextView;
    private TextView methodTextView;
    private TextView addressTextView;
    private TextView balanceTextView;
    private TextView statusTextView;

    private TextView contactTextView;
    private TextView noteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_details);

        Button printButton = findViewById(R.id.printButton);

        noteTextView = findViewById(R.id.noteTextView);


        fbTextView = findViewById(R.id.fbTextView);
        nameTextView = findViewById(R.id.nameTextView);
        methodTextView = findViewById(R.id.methodTextView);
        addressTextView = findViewById(R.id.addressTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        contactTextView = findViewById(R.id.contactTextView);
        statusTextView = findViewById(R.id.statusTextView);

        // Retrieve data passed through Intent
        Intent intent = getIntent();
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = intent.getStringExtra("name");
                String address = intent.getStringExtra("address");
                String contact = intent.getStringExtra("contact");
                // Prepare the formatted details

                // Save the formatted details to a file and download
                saveAndDownloadToFile(name, address, contact);
            }
        });
        if (intent != null) {


            String fb = intent.getStringExtra("fb");
            String name = intent.getStringExtra("name");
            String method = intent.getStringExtra("method");
            String address = intent.getStringExtra("address");
            double balance = intent.getDoubleExtra("balance", 0.0);
            String status = intent.getStringExtra("status");
            String contact = intent.getStringExtra("contact");
            String note = intent.getStringExtra("note");

            // Populate UI elements with the data
            fbTextView.setText(fb);
            nameTextView.setText(name);
            methodTextView.setText(method);
            addressTextView.setText(address);
            balanceTextView.setText(String.valueOf((int) balance));

            // balanceTextView.setText((int) balance);
            statusTextView.setText(status);
            contactTextView.setText( contact);
            noteTextView.setText(note);
        }

        // Back Button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(BuyerDetailsActivity.this, BuyerActivity.class);
                startActivity(intent1);
            }
        });
        String id = intent.getStringExtra("id");
        // Edit Button
        Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the "Edit" button is clicked, navigate to BuyerEditDetailsActivity
                Intent editIntent = new Intent(BuyerDetailsActivity.this, BuyerEditDetailsActivity.class);
                editIntent.putExtra("fb", fbTextView.getText().toString());
                editIntent.putExtra("name", nameTextView.getText().toString());
                editIntent.putExtra("method", methodTextView.getText().toString());
                editIntent.putExtra("address", addressTextView.getText().toString());
                editIntent.putExtra("balance", Double.parseDouble(balanceTextView.getText().toString().replace("Balance: ", "")));
                editIntent.putExtra("status", statusTextView.getText().toString());
                editIntent.putExtra("contact",contactTextView.getText().toString());
                editIntent.putExtra("note",noteTextView.getText().toString());
                editIntent.putExtra("id",id);
                startActivity(editIntent);
            }
        });
    }
    private void saveAndDownloadToFile(String name, String address, String contact) {
        try {
            XWPFDocument document = new XWPFDocument();

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setFontSize(40);
            run.setFontFamily("Sylfaen");
            run.setText("Name: "+name+ "\nAddress: "+address+"\nContact: "+contact );

            // Save the document to the Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, "printed_details.docx");

            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();

            // Initiate download using a FileProvider
            Uri contentUri = FileProvider.getUriForFile(this, "com.example.lotusjapansurplus.fileprovider", file);

            Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
            downloadIntent.setData(contentUri);
            downloadIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (downloadIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(downloadIntent);
            } else {
                Toast.makeText(this, "No app to handle this action", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }
    }


}
