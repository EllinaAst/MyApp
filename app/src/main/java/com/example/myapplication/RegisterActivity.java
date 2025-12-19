package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private Button registerBtn, backBtn;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);

        // ----------- ПОЛЯ ИЗ XML -----------
        emailInput = findViewById(R.id.et_email);
        lastNameInput = findViewById(R.id.et_last_name);
        firstNameInput = findViewById(R.id.et_first_name);
        passwordInput = findViewById(R.id.et_password);
        registerBtn = findViewById(R.id.btn_register);
        backBtn = findViewById(R.id.btn_back);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        registerBtn.setOnClickListener(v -> registerUser());

        // ------------ КНОПКА НАЗАД ------------
        backBtn.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    usersRef.child(uid).child("firstName").setValue(firstName);
                    usersRef.child(uid).child("lastName").setValue(lastName);
                    usersRef.child(uid).child("email").setValue(email);
                    usersRef.child(uid).child("role").setValue("user");

                    Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
