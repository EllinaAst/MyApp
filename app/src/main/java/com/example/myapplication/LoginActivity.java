package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister, btnBack;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(view -> loginUser());

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnBack.setOnClickListener(v -> finish());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthInvalidUserException) {
                            showToast("Пользователя с такой почтой не существует");
                        }
                        else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            showToast("Неверная электронная почта или пароль");
                        }
                        else {
                            showToast("Ошибка: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * Проверка роли после входа
     */
    private void checkUserRole() {

        String uid = auth.getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    showToast("Ошибка загрузки профиля");
                    return;
                }

                String role = snapshot.child("role").getValue(String.class);

                if ("admin".equals(role)) {

                    // ---------------- Админ ----------------
                    Intent intent = new Intent(LoginActivity.this, AdminPanelActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                // ---------------- Обычный пользователь ----------------
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);

                Intent intent = new Intent(LoginActivity.this, ThemeSelectionActivity.class);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);

                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Ошибка подключения к базе данных");
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
