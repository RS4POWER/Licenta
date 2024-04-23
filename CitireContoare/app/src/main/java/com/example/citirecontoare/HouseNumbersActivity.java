package com.example.citirecontoare;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

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
                                houseButton.setOnClickListener(v -> {
                                    // Acțiunea când se apasă pe butonul casei
                                    // Aici poți deschide o nouă activitate sau fragment cu detaliile casei
                                    // folosind houseNumber sau document.getId() ca identificator
                                    Log.w(TAG, "S-a deschis numarul de casa."+ houseNumber);
                                });
                                housesContainer.addView(houseButton);
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting house numbers: ", task.getException());
                    }
                });
    }


}

