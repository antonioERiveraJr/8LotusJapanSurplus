package com.example.lotusjapansurplus.transactions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotusjapansurplus.ExcelDataAdapter;
import com.example.lotusjapansurplus.R;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TransactionShowExcelData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_show_excel_data);

        String[] buyers = getIntent().getStringExtra("BUYER_DATA").split("\t     \n");
        String[] items = getIntent().getStringExtra("ITEM_DATA").split("\t     \n");
        String[] prices = getIntent().getStringExtra("PRICE_DATA").split("\t     \n");

        TableLayout tableLayout = findViewById(R.id.tableLayout);

        for (int i = 0; i < buyers.length; i++) {
            TableRow tableRow = new TableRow(this);

            TextView buyerTextView = createTextViewWithSpace(tableLayout.getContext(), buyers[i], 5);
            TextView itemTextView = createTextViewWithSpace(tableLayout.getContext(), items[i], 5);
            TextView priceTextView = createTextViewWithSpace(tableLayout.getContext(), prices[i], 5);

            tableRow.addView(buyerTextView);
            tableRow.addView(itemTextView);
            tableRow.addView(priceTextView);

            tableLayout.addView(tableRow);
        }

        // Calculate the total price
        double totalPrice = calculateTotalPrice(prices);

        TextView totalTextView = new TextView(this);
        totalTextView.setText("Total Price: " + totalPrice);
        tableLayout.addView(totalTextView);

        // Back Button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionShowExcelData.this, TransactionActivity.class);
            startActivity(intent);
        });
    }

    private TextView createTextViewWithSpace(Context context, String text, int spaces) {
        String space = new String(new char[spaces]).replace("\0", " ");
        TextView textView = new TextView(context);
        textView.setText(text + space); // Appending the spaces to the text
        return textView;
    }

    private double calculateTotalPrice(String[] prices) {
        double totalPrice = 0.0;

        for (String priceData : prices) {
            String[] priceStrings = priceData.split("\n");
            for (String price : priceStrings) {
                double priceValue = Double.parseDouble(price.trim()); // Parse each individual price
                totalPrice += priceValue;
            }
        }
        return totalPrice;
    }
}
