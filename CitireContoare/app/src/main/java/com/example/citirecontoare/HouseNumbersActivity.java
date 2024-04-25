package com.example.citirecontoare;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HouseNumbersActivity extends AppCompatActivity {

    private LinearLayout housesContainer;
    private TextView zoneNameTextView;
    private FirebaseFirestore db;

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_numbers);

        housesContainer = findViewById(R.id.housesContainer);
        zoneNameTextView = findViewById(R.id.zoneNameTextView);
        String zoneName = getIntent().getStringExtra("zoneName");
        zoneNameTextView.setText(zoneName);


        db = FirebaseFirestore.getInstance();
        loadHouseNumbers(zoneName);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Închide activitatea curentă
            }
        });
       // for(int i=0;i<=50;i++)
       // loadHouseNumbers(zoneName);


    }

    private void loadHouseNumbers(String zoneName) {
        Log.w(TAG, "S-a apelat loadHouseNumbers----------.");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("zones").document(zoneName).collection("numereCasa")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Crează o listă pentru a stoca numerele caselor împreună cu documentele asociate
                        List<DocumentSnapshot> houseDocumentsList = new ArrayList<>();

                        // Adaugă toate documentele în listă
                        houseDocumentsList.addAll(task.getResult().getDocuments());

                        // Sortează lista folosind numărul casei
                        Collections.sort(houseDocumentsList, (d1, d2) -> {
                            Long num1 = d1.getLong("Numar casa");
                            Long num2 = d2.getLong("Numar casa");
                            return num1.compareTo(num2);
                        });

                        // Adaugă butoanele în interfață în ordinea corectă
                        for (DocumentSnapshot document : houseDocumentsList) {
                            Long houseNumber = document.getLong("Numar casa");
                            if (houseNumber != null) {
                                Button houseButton = new Button(HouseNumbersActivity.this);
                                houseButton.setText("Casa " + houseNumber);
                                houseButton.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));

                                // Aici setezi un tag pentru buton, care poate fi folosit in listener pentru a identifica care buton a fost apasat
                                houseButton.setTag(houseNumber);

                                houseButton.setOnClickListener(v -> {
                                    // Acțiunea când se apasă pe butonul casei
                                    // Aici pot deschide o nouă activitate sau fragment cu detaliile casei
                                    // folosind houseNumber sau document.getId() ca identificator

                                    Log.w(TAG, "S-a deschis numarul de casa." + houseNumber);
                                    Long selectedHouseNumber = (Long) v.getTag();
                                    openHouseDetails(selectedHouseNumber);
                                });

                                housesContainer.addView(houseButton);
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting house numbers: ", task.getException());
                    }
                });
    }


    public void openHouseDetails(Long houseNumber) {
        Intent intent = new Intent(HouseNumbersActivity.this, HouseDetailsActivity.class);
        intent.putExtra("HOUSE_NUMBER", houseNumber);
        startActivity(intent);
    }



}


/* codul dinainte de sortare

private void loadHouseNumbers(String zoneName) {
        Log.w(TAG, "S-a apleat loadHouseNumbers----------.");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("zones").document(zoneName).collection("numereCasa")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (DocumentSnapshot document : task.getResult()) {
                            // Presupunem că există un câmp 'Numar casa' care este un număr
                            Long houseNumber = document.getLong("Numar casa");
                            if (houseNumber != null) {
                                Button houseButton = new Button(HouseNumbersActivity.this);
                                houseButton.setText("Casa " + houseNumber);
                                houseButton.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));

                                // Aici setezi un tag pentru buton, care poate fi folosit in listener pentru a identifica care buton a fost apasat
                                houseButton.setTag(houseNumber);

                                houseButton.setOnClickListener(v -> {
                                    // Acțiunea când se apasă pe butonul casei
                                    // Aici poți deschide o nouă activitate sau fragment cu detaliile casei
                                    // folosind houseNumber sau document.getId() ca identificator
                                    Log.w(TAG, "S-a deschis numarul de casa."+ houseNumber);

                                    Long selectedHouseNumber = (Long) v.getTag();
                                    openHouseDetails(selectedHouseNumber);
                                });
                                housesContainer.addView(houseButton);
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting house numbers: ", task.getException());
                    }
                });
    }


 */