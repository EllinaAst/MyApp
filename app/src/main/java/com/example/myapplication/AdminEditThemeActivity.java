package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminEditThemeActivity extends AppCompatActivity {

    private EditText titleInput, theoryInput, examplesInput;
    private Button saveBtn;

    private DatabaseReference themesRef;
    private String themeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_theme);

        // 🟣 Toolbar
        Toolbar toolbar = findViewById(R.id.adminThemesToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 🟣 Поля
        titleInput = findViewById(R.id.editThemeTitle);
        theoryInput = findViewById(R.id.editThemeTheory);
        examplesInput = findViewById(R.id.editThemeExamples);
        saveBtn = findViewById(R.id.btnSaveTheme);

        themesRef = FirebaseDatabase.getInstance().getReference("themes");

        themeId = getIntent().getStringExtra("themeId");

        if (themeId == null) {
            Toast.makeText(this, "Тема не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTheme();

        saveBtn.setOnClickListener(v -> saveTheme());
    }

    private void loadTheme() {
        themesRef.child(themeId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String t = snapshot.child("title").getValue(String.class);
                String th = snapshot.child("theory").getValue(String.class);
                String ex = snapshot.child("examples").getValue(String.class);

                titleInput.setText(t);
                theoryInput.setText(th);
                examplesInput.setText(ex);
            }
        });
    }

    private void saveTheme() {
        String title = titleInput.getText().toString().trim();
        String theory = theoryInput.getText().toString().trim();
        String examples = examplesInput.getText().toString().trim();

        if (title.isEmpty() || theory.isEmpty() || examples.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Theme theme = new Theme(themeId, title, theory, examples);

        themesRef.child(themeId).setValue(theme)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Тема обновлена!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                );
    }
}
