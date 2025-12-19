package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        setupStartButton();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Если гость — ничего не делаем (кнопка "Начать" поведёт его к темам)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        // Если авторизован — проверяем роль
        checkUserRole();
    }

    // ================= Работа кнопки «Начать» =================
    private void setupStartButton() {
        Button btnStart = findViewById(R.id.startButton);

        btnStart.setOnClickListener(v -> {

            // Гость → сразу в темы
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(this, ThemeSelectionActivity.class));
                return;
            }

            // Авторизован → проверка роли (повторно)
            checkUserRole();
        });
    }

    // ================= Проверка роли пользователя =================
    private void checkUserRole() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    String role = snapshot.child("role").getValue(String.class);

                    if ("admin".equals(role)) {
                        startActivity(new Intent(this, AdminPanelActivity.class));
                    } else {
                        startActivity(new Intent(this, ThemeSelectionActivity.class));
                    }

                    finish();
                });
    }
}
