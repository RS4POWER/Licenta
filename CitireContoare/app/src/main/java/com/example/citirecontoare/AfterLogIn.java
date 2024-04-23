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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class AfterLogIn extends AppCompatActivity {
    private Button zoneButton1;
    private Button zoneButton2;
    private Button LogOff;
    private static final String TAG = "AfterLogin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_log_in);

        TextView titleTextView = findViewById(R.id.title_text_view);
        zoneButton1 = findViewById(R.id.zoneButton1);
        zoneButton2 = findViewById(R.id.zoneButton2);
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
                        titleTextView.setText("Zonele de citire:\n" + zoneAsignate);

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
    }
}
// aici e prima incercare cu zonele in functie de utilizator; merge!
