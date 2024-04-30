package com.example.citirecontoare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private ImageButton backButton,manualModifyButton,  previousYearButton, nextYearButton;
    private  Button nextMonthButton,previousMonthButton;


    // Adaug restul EditText-urilor pentru consum, stare, data citirii, etc.

    private int currentYear;
    private int currentMonth;
    private Long houseNumber;

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

    private void saveHouseDetails() {
        // Obține datele din EditText-uri și le salvează în Firestore
        String marca = marcaEditText.getText().toString();
        String seria = seriaEditText.getText().toString();
        String diametru = diametruEditText.getText().toString();
        String dataInstalare = dataInstalareEditText.getText().toString();
        // Obține restul datelor din EditText-uri

        if (!isValidNumber(diametru)) {
            Toast.makeText(this, "Doar cifre sunt acceptate pentru diametru.", Toast.LENGTH_SHORT).show();
            return;
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
            Toast.makeText(this, "Consumația trebuie să fie un număr valid.", Toast.LENGTH_SHORT).show();
            return;
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


}


