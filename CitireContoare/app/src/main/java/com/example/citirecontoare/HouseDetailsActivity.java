package com.example.citirecontoare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HouseDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView ownerNameTextView, houseNumberTextView;
    private EditText marcaEditText, seriaEditText, diametruEditText, dataInstalareEditText,stareApometruEditText,consumMcEditText,datacitiriiEditText;
    private boolean isInEditMode = false;
    private ImageButton backButton,manualModifyButton,  previousYearButton, nextYearButton,takePhotoButton;
    private  Button nextMonthButton,previousMonthButton;


    // Adaug restul EditText-urilor pentru consum, stare, data citirii, etc.

    private int currentYear;
    private int currentMonth;
    private Long houseNumber;

    boolean succes;

    public  String[] monthNames = {"Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
            "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_details);

        // Inițializează componentele UI

        ownerNameTextView = findViewById(R.id.ownerNameTextView);
        houseNumberTextView = findViewById(R.id.houseNumberTextView);
        marcaEditText = findViewById(R.id.marcaEditText);
        seriaEditText = findViewById(R.id.seriaEditText);
        diametruEditText = findViewById(R.id.diametruEditText);
        dataInstalareEditText = findViewById(R.id.datainstalareEditText);
        manualModifyButton = findViewById(R.id.manualModifyButton);
        stareApometruEditText = findViewById(R.id.stareApometruEditText);
        consumMcEditText = findViewById(R.id.consumMcEditText);
        datacitiriiEditText = findViewById(R.id.datacitiriiEditText);
        backButton = findViewById(R.id.backButton);
        ImageButton previousYearButton = findViewById(R.id.previousYearButton);
        ImageButton nextYearButton = findViewById(R.id.nextYearButton);
        Button previousMonthButton = findViewById(R.id.previousMonthButton);
        Button nextMonthButton = findViewById(R.id.nextMonthButton);
        ImageButton takePhotoButton = findViewById(R.id.takePhotoButton);

        // Inițializează restul EditText-urilor aici

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);



        updateDateDisplay();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Închide activitatea curentă
            }

        });


        previousYearButton.setOnClickListener(v -> {
            currentYear--;
            updateDateDisplay();
            loadApometruDetails(houseNumber, currentYear, currentMonth);
            loadHouseDetails(houseNumber);
        });

        nextYearButton.setOnClickListener(v -> {
            currentYear++;
            updateDateDisplay();
            loadApometruDetails(houseNumber, currentYear, currentMonth);
            loadHouseDetails(houseNumber);
        });

        previousMonthButton.setOnClickListener(v -> {
            if (currentMonth > 0) {
                currentMonth--;
            } else {
                currentMonth = 11;
                currentYear--;
            }
            updateDateDisplay();
            loadApometruDetails(houseNumber, currentYear, currentMonth);
            loadHouseDetails(houseNumber);
        });

        nextMonthButton.setOnClickListener(v -> {
            if (currentMonth < 11) {
                currentMonth++;
            } else {
                currentMonth = 0;
                currentYear++;
            }
            updateDateDisplay();
            loadApometruDetails(houseNumber, currentYear, currentMonth);
            loadHouseDetails(houseNumber);
        });


        toggleEditMode(false);
        manualModifyButton.setOnClickListener(v -> {
            isInEditMode = !isInEditMode;

            // Actualizează iconița butonului
            if (isInEditMode) {
                manualModifyButton.setBackgroundResource(R.drawable.baseline_done_outline_24);
            } else {
                manualModifyButton.setBackgroundResource(R.drawable.baseline_edit_note_24);
                saveHouseDetails(); // Salvează datele când ieși din modul de editarehoue
                saveApometruDetails();
            }
            toggleEditMode(isInEditMode);
        });


        // Obține numărul casei și numele proprietarului din intent
        Intent intent = getIntent();
         houseNumber = intent.getLongExtra("HOUSE_NUMBER", -1);
        if(houseNumber == -1) {
            Toast.makeText(this, "Numărul casei nu a fost transmis corect.", Toast.LENGTH_SHORT).show();
            finish(); // Închide activitatea dacă numărul casei nu este valid
        }

        // Setează numărul casei în TextView
        houseNumberTextView.setText(String.valueOf("NR "+houseNumber));

        // Aduce detalii specifice casei din Firestore
        loadHouseDetails(houseNumber);
        loadApometruDetails(houseNumber, currentYear, currentMonth);



        intent = getIntent();
         houseNumber = intent.getLongExtra("HOUSE_NUMBER", -1);

        if(houseNumber != -1) {
            loadApometruDetails(houseNumber, currentYear, currentMonth);
        } else {
            Toast.makeText(this, "Numărul casei nu a fost transmis corect.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //OCR

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });


    }

    private void loadHouseDetails(Long houseNumber) {
        DocumentReference houseRef = db.collection("zones")
                .document("Iacobini") // Acesta ar trebui să fie ID-ul zonei, înlocuiește-l cu valoarea corectă
                .collection("numereCasa")
                .document("Numarul " + houseNumber);

        houseRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                // Actualizează interfața cu datele casei
                ownerNameTextView.setText(documentSnapshot.getString("Proprietar"));
                marcaEditText.setText(documentSnapshot.getString("Apometru marca"));
                seriaEditText.setText(documentSnapshot.getString("Seria"));
                diametruEditText.setText(documentSnapshot.getLong("Diametru apometru").toString());
                dataInstalareEditText.setText(documentSnapshot.getString("Instalat la"));

                // Completează restul EditText-urilor cu datele din document
            } else {
                Toast.makeText(this, "Detalii despre casă nu au fost găsite.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Eroare la încărcarea detaliilor casei.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadApometruDetails(Long houseNumber, int year, int month) {
        String monthName = monthNames[month];
        DocumentReference monthRef = db.collection("zones")
                .document("Iacobini")
                .collection("numereCasa")
                .document("Numarul " + houseNumber)
                .collection("consumApa")
                .document(String.valueOf(year))
                .collection("lunile")
                .document(monthName);

        monthRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Verifică și tratează 'Starea Apometrului' ca număr
                Long stareaApometrului = documentSnapshot.getLong("Starea Apometrului");
                if (stareaApometrului != null) {
                    stareApometruEditText.setText(String.valueOf(stareaApometrului)); // Convertim la String
                } else {
                    stareApometruEditText.setText("Nedefinit");
                }

                // Procesează restul datelor ca string-uri
                consumMcEditText.setText(String.valueOf(documentSnapshot.getLong("Consumatia mc"))); // Convertim numărul la String
                datacitiriiEditText.setText(documentSnapshot.getString("Data citire"));

            } else {
                Toast.makeText(this, "Nu există înregistrări pentru " + monthName + " " + year, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Eroare la încărcarea datelor pentru " + monthName + " " + year, Toast.LENGTH_SHORT).show();
        });
    }





    private void toggleEditMode(boolean isInEditMode) {
        marcaEditText.setEnabled(isInEditMode);
        seriaEditText.setEnabled(isInEditMode);
        diametruEditText.setEnabled(isInEditMode);
        dataInstalareEditText.setEnabled(isInEditMode);
        stareApometruEditText.setEnabled(isInEditMode);
        consumMcEditText.setEnabled(isInEditMode);
        datacitiriiEditText.setEnabled(isInEditMode);
        // Activează sau dezactivează restul EditText-urilor în funcție de isInEditMode
    }

    @SuppressLint("SuspiciousIndentation")
    private void saveHouseDetails() {
        // Obține datele din EditText-uri și le salvează în Firestore
        String marca = marcaEditText.getText().toString();
        String seria = seriaEditText.getText().toString();
        String diametru = diametruEditText.getText().toString();
        String dataInstalare = dataInstalareEditText.getText().toString();
        // Obține restul datelor din EditText-uri

        if (!isValidNumber(diametru)) {
            succes = false;
            Toast.makeText(this, "Doar cifre sunt acceptate pentru diametru.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            succes=true;
        }

        // Referința la documentul Firestore al casei
        Long houseNumber = getIntent().getLongExtra("HOUSE_NUMBER", -1);
        DocumentReference houseRef = db.collection("zones").document("Iacobini")
                .collection("numereCasa").document("Numarul " + houseNumber);

        // Map pentru a stoca datele
        Map<String, Object> houseDetails = new HashMap<>();
        houseDetails.put("Apometru marca", marca);
        houseDetails.put("Seria", seria);
        houseDetails.put("Diametru apometru", Long.parseLong(diametru)); // asigură-te că acesta este un număr
        houseDetails.put("Instalat la", dataInstalare);
        // Adaugă restul datelor în map

        // Actualizează în Firestore
        houseRef.update(houseDetails)
                .addOnSuccessListener(aVoid -> {
                    if(succes!=false)
                    Toast.makeText(HouseDetailsActivity.this, "Datele au fost salvate cu succes!", Toast.LENGTH_SHORT).show();
                    isInEditMode = false; // Dezactivează modul de editare după salvare
                    toggleEditMode(false); // Actualizează UI-ul
                    //manualModifyButton.setImageResource(R.drawable.baseline_edit_note_24); // Schimbă iconița înapoi
                })
                .addOnFailureListener(e -> Toast.makeText(HouseDetailsActivity.this, "Eroare la salvarea datelor.", Toast.LENGTH_SHORT).show());

        isInEditMode = false;
        toggleEditMode(false);

    }


    private void saveApometruDetails() {
        Long houseNumber = getIntent().getLongExtra("HOUSE_NUMBER", -1);
        String stareApometru = stareApometruEditText.getText().toString();
        String consumMc = consumMcEditText.getText().toString();
        String dataCitirii = datacitiriiEditText.getText().toString();

        if (!isValidNumber(consumMc)) {
            succes=false;
            Toast.makeText(this, "Consumația trebuie să fie un număr valid.", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            succes=true;
        }

        double consumValue = Double.parseDouble(consumMc);
        long consumInt = (long) consumValue;

        DocumentReference monthRef = db.collection("zones")
                .document("Iacobini")
                .collection("numereCasa")
                .document("Numarul " + houseNumber)
                .collection("consumApa")
                .document(String.valueOf(currentYear))
                .collection("lunile")
                .document(monthNames[currentMonth]);

        Map<String, Object> apometruDetails = new HashMap<>();
        apometruDetails.put("Starea Apometrului", Long.parseLong(stareApometru));
        apometruDetails.put("Consumatia mc", consumInt);
        apometruDetails.put("Data citire", dataCitirii);

        monthRef.update(apometruDetails)
                .addOnSuccessListener(aVoid -> {
                    isInEditMode = false;
                    toggleEditMode(false);
                })
                .addOnFailureListener(e -> Toast.makeText(HouseDetailsActivity.this, "Eroare la actualizarea datelor apometrului.", Toast.LENGTH_SHORT).show());
    }


    private boolean isValidNumber(String numberStr) {
        try {
            Double.parseDouble(numberStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void updateDateDisplay() {
        String monthName = monthNames[currentMonth];
        String displayText = monthName + ", " + currentYear;
        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(displayText);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private void requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is necessary to use this feature", Toast.LENGTH_LONG).show();
            }
        }
    }

    //OCR
    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Log.d("OCR", "Image captured and received in the app");
                recognizeText(imageBitmap);
            } else {
                Log.d("OCR", "No image data received");
            }
        } else {
            Log.d("OCR", "Camera activity did not return RESULT_OK");
        }
    }



    private void recognizeText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(this::processTextRecognitionResult)
                .addOnFailureListener(e -> {
                    Log.e("OCR", "OCR Failed: " + e.getMessage());
                    Toast.makeText(HouseDetailsActivity.this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }




    private void processTextRecognitionResult(Text result) {
        String resultText = result.getText();
        Log.d("OCR", "OCR Result: " + resultText);
        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Log.d("OCR", "Block text: " + blockText);
            if (blockText.matches("\\d+")) { // Regex pentru a verifica dacă textul este numeric
                stareApometruEditText.setText(blockText);
                Log.d("OCR", "Numeric text set in EditText: " + blockText);
                break;
            }
            else
            if (!blockText.matches("\\d+")) {
                Log.d("OCR", "Non-numeric text detected: " + blockText);
            }

        }
        if(resultText.isEmpty()) {
            Log.d("OCR", "No text recognized");
        }
    }



}


