package com.example.citirecontoare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


public class AfterLogIn extends AppCompatActivity {
    private Button zoneButton1;
    private Button zoneButton2;
    private Button LogOff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_log_in);

        TextView titleTextView = findViewById(R.id.title_text_view);
        zoneButton1 = findViewById(R.id.zoneButton1);
        zoneButton2 = findViewById(R.id.zoneButton2);
        LogOff = findViewById(R.id.LogOff);

        // Obține informațiile despre utilizatorul curent (zonele asignate)

        // Setează textul sau imaginea pentru fiecare buton în funcție de zonele asignate

        zoneButton1.setText("Zona 1");
        zoneButton2.setText("Zona 2");
        LogOff.setText("LogOff");

        // Adaugă un listener pentru butoane
        zoneButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deschide o altă activitate sau fragment pentru zona 1
            }
        });

        zoneButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deschide o altă activitate sau fragment pentru zona 2
            }
        });

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
