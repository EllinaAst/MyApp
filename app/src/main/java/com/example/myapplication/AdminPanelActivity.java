package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminPanelActivity extends AppCompatActivity {

    private Button btnUsers, btnThemes, btnAddTheme, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        btnUsers = findViewById(R.id.btn_admin_users);
        btnThemes = findViewById(R.id.btn_admin_themes);
        btnAddTheme = findViewById(R.id.btn_admin_add_theme);
        btnLogout = findViewById(R.id.btn_admin_logout);

        btnUsers.setOnClickListener(v ->
                startActivity(new Intent(AdminPanelActivity.this, AdminUsersActivity.class))
        );

        btnThemes.setOnClickListener(v ->
                startActivity(new Intent(AdminPanelActivity.this, AdminThemesActivity.class))
        );

        btnAddTheme.setOnClickListener(v ->
                startActivity(new Intent(AdminPanelActivity.this, AdminCreateThemeActivity.class))
        );

        // 🔥 ВЫХОД И ПЕРЕХОД НА СТРАНИЦУ ПРИВЕТСТВИЯ
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(AdminPanelActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        });
    }
}
