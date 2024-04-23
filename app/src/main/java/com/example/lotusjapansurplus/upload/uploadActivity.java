package com.example.lotusjapansurplus.upload;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.lotusjapansurplus.R;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class uploadActivity extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Trigger file picker
        pickExcelFile();
    }

    private void pickExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // MIME type for Excel files
        startActivityForResult(intent, PICK_EXCEL_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_EXCEL_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri excelUri = data.getData();
            if (excelUri != null) {
                try {
                    InputStream stream = getContentResolver().openInputStream(excelUri);
                    Workbook workbook = WorkbookFactory.create(stream);
                    Sheet sheet = workbook.getSheetAt(0);

                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            // Process each cell
                            String cellValue = cell.toString();
                            Toast.makeText(this, cellValue, Toast.LENGTH_SHORT).show();
                        }
                    }

                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}