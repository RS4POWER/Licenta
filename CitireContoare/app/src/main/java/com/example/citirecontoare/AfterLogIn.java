package com.example.citirecontoare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class AfterLogIn extends AppCompatActivity {
    private Button zoneButton1;
    private Button zoneButton2;
    private Button LogOff;

   // private  Button AdaugareDate;
    private static final String TAG = "AfterLogin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_log_in);

        TextView titleTextView = findViewById(R.id.title_text_view);
        zoneButton1 = findViewById(R.id.zoneButton1);
        zoneButton2 = findViewById(R.id.zoneButton2);
      //  AdaugareDate = findViewById(R.id.AdaugaDate);
        LogOff = findViewById(R.id.LogOff);

        // Obține informațiile despre utilizatorul curent (zonele asignate)
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userUID = firebaseAuth.getCurrentUser().getUid();
        Log.d(TAG,"-------------"+userUID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userUID);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String zoneAsignate = documentSnapshot.getString("zoneAsignate");
                    Log.d(TAG,"oooooooooooooooooooo"+zoneAsignate);

                    if (zoneAsignate != null && !zoneAsignate.isEmpty()) {
                        // Afișează zonele asignate în TextView
                     //   titleTextView.setText("Zonele de citire:\n" + zoneAsignate);

                        // Împarte zonele asignate într-un array de string-uri
                        String[] zoneArray = zoneAsignate.split(";");

                        // Setează textul butoanelor cu zonele asignate
                        if (zoneArray.length >= 1) {
                            zoneButton1.setText(zoneArray[0]);
                        }
                        if (zoneArray.length >= 2) {
                            zoneButton2.setText(zoneArray[1]);
                        }
                    } else {
                        titleTextView.setText("Nu există zone asignate pentru acest utilizator.");
                    }
                } else {
                    Log.d(TAG, "Document does not exist");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting document: " + e);
            }
        });

        // Setează textul butonului de log off
        LogOff.setText("Log Off");

        // Adaugă un listener pentru butonul de log off
        LogOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AfterLogIn.this, LoginActivity.class));
                finish();
            }
        });

        zoneButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AfterLogIn.this, HouseNumbersActivity.class);
                intent.putExtra("zoneName", zoneButton1.getText().toString());
                startActivity(intent);


            }
        });

        zoneButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AfterLogIn.this, HouseNumbersActivity.class);
                intent.putExtra("zoneName", zoneButton2.getText().toString());
                startActivity(intent);
            }
        });

//        AdaugareDate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                FirebaseFirestore db = FirebaseFirestore.getInstance();
//                DocumentReference zoneRef = db.collection("zones").document("Iacobini");
//                WriteBatch batch = db.batch();
//
//                // De exemplu, dacă vrei să adaugi numere de casă de la 3 la 80
//                for (int i = 21; i <= 30; i++) {
//                    // Adaugă date generale despre casă
//                    DocumentReference houseRef = zoneRef.collection("numereCasa").document("Numarul " + i);
//                    Map<String, Object> houseData = new HashMap<>();
//                    houseData.put("Numar casa", i);
//                    houseData.put("Instalat la", ""); // completare conform necesității
//                    houseData.put("Proprietar", ""); // completare conform necesității
//                    houseData.put("Seria", ""); // completare conform necesității
//                    houseData.put("Apometru marca", ""); // completare conform necesității
//                    houseData.put("Diametru apometru", 20); // default 20
//                    batch.set(houseRef, houseData);
//
//                    // Adaugă date despre consumul de apă pentru anul 2023
//                    DocumentReference yearRef = houseRef.collection("consumApa").document("2023");
//                    CollectionReference monthsRef = yearRef.collection("lunile");
//                    for (String month : new String[]{"Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"}) {
//                        DocumentReference monthRef = monthsRef.document(month);
//                        Map<String, Object> monthData = new HashMap<>();
//                        monthData.put("Consumatia mc", 0); // presupunem 0 consum inițial
//                        monthData.put("Data citire", ""); // data va fi adăugată la citire
//                        monthData.put("Observatii", ""); // completare conform necesității
//                        monthData.put("Starea Apometrului", 0); // presupunem stare bună inițială
//                        batch.set(monthRef, monthData);
//                    }
//                }
//
//                // Execută batch-ul
//                batch.commit().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d("Firestore", "Casele au fost adăugate cu succes!");
//                    } else {
//                        Log.e("Firestore", "Eroare la adăugarea caselor", task.getException());
//                    }
//                });
//
//            }
//        });


    }


}
// aici e prima incercare cu zonele in functie de utilizator; merge!
