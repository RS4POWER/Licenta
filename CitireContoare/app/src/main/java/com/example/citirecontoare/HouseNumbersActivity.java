package com.example.citirecontoare;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HouseNumbersActivity extends AppCompatActivity {

    String zoneName;
    private LinearLayout housesContainer;
    private TextView zoneNameTextView;
    private FirebaseFirestore db;

    private Button  downloadButton;
    private ImageButton backButton;

    public  String[] monthNames = {"Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
            "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_numbers);

        housesContainer = findViewById(R.id.housesContainer);
        zoneNameTextView = findViewById(R.id.zoneNameTextView);
         zoneName = getIntent().getStringExtra("zoneName");
        zoneNameTextView.setText(zoneName);
        downloadButton=findViewById(R.id.downloadButton);

        db = FirebaseFirestore.getInstance();
        loadHouseNumbers(zoneName);

        backButton = findViewById(R.id.backButton);

        Log.d("sa vedem zona-", zoneName);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Închide activitatea curentă
            }

        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifică permisiunile înainte de a începe descărcarea
                if (ContextCompat.checkSelfPermission(HouseNumbersActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Dacă nu sunt permisiunile necesare, cere-le
                    ActivityCompat.requestPermissions(HouseNumbersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    // Dacă permisiunile sunt deja acordate, începe descărcarea datelor
                    downloadData(v);
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permisiunea a fost acordată
            View view = new View(this);
            downloadData(view);
        } else {
            // Permisiunea a fost refuzată
            Toast.makeText(this, "Permisiunea pentru scriere pe stocare externă este necesară pentru a descărca datele.", Toast.LENGTH_LONG).show();
        }
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Concatenează "Numarul " cu numărul casei pentru a obține numele documentului corect
        String documentName = "Numarul " + houseNumber;
        DocumentReference houseRef = db.collection("zones")
                .document(zoneName)
                .collection("numereCasa")
                .document(documentName);

        houseRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Intent intent = new Intent(HouseNumbersActivity.this, HouseDetailsActivity.class);
                intent.putExtra("HOUSE_NUMBER", houseNumber);
                intent.putExtra("ZONE_NAME", zoneName);
                String ownerName = documentSnapshot.getString("Proprietar"); // presupunând că acesta este numele câmpului
                intent.putExtra("OWNER_NAME", ownerName); // Pasăm numele proprietarului către noua activitate
                Log.d(TAG, "numar casa: " + houseNumber + " proprietar: " + ownerName);
                startActivity(intent);
            } else {
                Toast.makeText(HouseNumbersActivity.this, "Detalii casa nu au fost gasite.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(HouseNumbersActivity.this, "Eroare la obținerea detaliilor casei.", Toast.LENGTH_SHORT).show();
        });
    }

    public void downloadData(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<Map<String, Object>> houseDetailsList = new ArrayList<>();

        db.collection("zones").document(zoneName).collection("numereCasa")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Map<String, Object>>> subTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> houseData = new HashMap<>(document.getData());
                            String docId = document.getId();

                            List<Task<Map<String, Object>>> monthTasks = new ArrayList<>();
                            for (String month : monthNames) {
                                Task<DocumentSnapshot> monthTask = db.collection("zones")
                                        .document(zoneName)
                                        .collection("numereCasa")
                                        .document(docId)
                                        .collection("consumApa")
                                        .document("2024")
                                        .collection("lunile")
                                        .document(month)
                                        .get();

                                // Ensure the continuation task returns a Map<String, Object>
                                Task<Map<String, Object>> continuationTask = monthTask.continueWith(task1 -> {
                                    DocumentSnapshot monthDoc = task1.getResult();
                                    if (monthDoc != null && monthDoc.exists()) {
                                        return Collections.singletonMap(month, monthDoc.getData());
                                    }
                                    return new HashMap<String, Object>();
                                });
                                monthTasks.add(continuationTask);
                            }

                            // When all month tasks complete, combine all month data into one map and add it to houseData
                            Tasks.whenAllSuccess(monthTasks).addOnSuccessListener(monthResults -> {
                                Map<String, Object> monthsData = new HashMap<>();
                                for (Object result : monthResults) {
                                    monthsData.putAll((Map<String, Object>) result);
                                }
                                houseData.put("consumApa", monthsData);
                                houseDetailsList.add(houseData);
                            });

                            subTasks.add(Tasks.whenAll(monthTasks).continueWith(task2 -> houseData));
                        }

                        // When all house tasks complete (all data aggregated), create the JSON file
                        Tasks.whenAllSuccess(subTasks).addOnSuccessListener(houseDetails -> {
                            try {
                                createJsonFile(houseDetailsList);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        Log.d("DownloadData", "Error getting documents: ", task.getException());
                    }
                });
    }




    private void createJsonFile(ArrayList<Map<String, Object>> dataList) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();  // Use GsonBuilder to enable pretty printing
        String jsonString = gson.toJson(dataList);

        File file = new File(getExternalFilesDir(null), "HouseData.json");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonString);
            Toast.makeText(this, "Datele au fost salvate în " + file.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Eroare la salvarea datelor.", Toast.LENGTH_SHORT).show();
        }
    }

}



   /* private void addYear2024Data() {  metoda pentru adaugare de date in baza de date.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Obține referința la colecția de numere de casă
        CollectionReference housesRef = db.collection("zones").document("Iacobini").collection("numereCasa");

        housesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot houseDocument : task.getResult()) {
                    String houseId = houseDocument.getId();

                    // Referința pentru anul 2024 în colecția consumApa a fiecărei case
                    DocumentReference year2024Ref = housesRef.document(houseId).collection("consumApa").document("2024");

                    // Creează documente pentru fiecare lună din anul 2024
                    for (String month : new String[]{"Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"}) {
                        DocumentReference monthRef = year2024Ref.collection("lunile").document(month);

                        // Datele pe care dorești să le setezi pentru fiecare lună
                        Map<String, Object> monthData = new HashMap<>();
                        monthData.put("Consumatia mc", 0); // presupunem 0 consum inițial
                        monthData.put("Data citire", ""); // data va fi adăugată la citire
                        monthData.put("Observatii", ""); // completare conform necesității
                        monthData.put("Starea Apometrului", 0); // presupunem stare bună inițială

                        // Adaugă datele în batch
                        batch.set(monthRef, monthData);
                    }
                }

                // Execută batch-ul
                batch.commit().addOnCompleteListener(commitTask -> {
                    if (commitTask.isSuccessful()) {
                        Log.d("Firestore", "Date pentru anul 2024 au fost adăugate cu succes!");
                    } else {
                        Log.e("Firestore", "Eroare la adăugarea datelor pentru anul 2024", commitTask.getException());
                    }
                });
            } else {
                Log.e("Firestore", "Eroare la obținerea documentelor casei", task.getException());
            }
        });
    }



    }
}

*/
