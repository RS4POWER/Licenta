package com.example.citirecontoare;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        //signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
       // signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String  email = signupEmail.getText().toString().trim();
                String   password = signupPassword.getText().toString().trim();

                // Verificăm dacă adresa de email si parola sunt goale
                if (email.isEmpty() && password.isEmpty())  {
                    Toast.makeText(SignupActivity.this, "Va rog completati adresa de email si parola.", Toast.LENGTH_SHORT).show();
                    return; // Întrerupe funcția onClick pentru a evita continuarea operațiilor
                }
                // Verificăm dacă adresa de email este goală
                if (email.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Te rog introdu o adresă de email.", Toast.LENGTH_SHORT).show();
                    return; // Întrerupe funcția onClick pentru a evita continuarea operațiilor
                }

                // Verificăm dacă parola este goală
                if (password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Te rog introdu o parolă.", Toast.LENGTH_SHORT).show();
                    return; // Întrerupe funcția onClick pentru a evita continuarea operațiilor
                }



                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                FirebaseUser user = auth.getCurrentUser();
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Email sent.");
                                                    }
                                                }
                                            });
                                } else {
                                    if (password.length() < 6) {
                                        Toast.makeText(SignupActivity.this, "Password must have at least 6 characters.",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });


            }


        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }


        });




    }

}
