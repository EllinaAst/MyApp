package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminCreateThemeActivity extends AppCompatActivity {

    private EditText titleInput, theoryInput, examplesInput;
    private Button btnCreateTests;
    private DatabaseReference themesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_theme);

        Toolbar toolbar = findViewById(R.id.adminCreateThemeToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        titleInput = findViewById(R.id.createThemeTitle);
        theoryInput = findViewById(R.id.createThemeTheory);
        examplesInput = findViewById(R.id.createThemeExamples);
        btnCreateTests = findViewById(R.id.btnCreateTests);

        themesRef = FirebaseDatabase.getInstance().getReference("themes");

        btnCreateTests.setOnClickListener(v -> saveThemeAndGoToTests());
    }

    private void saveThemeAndGoToTests() {
        String title = titleInput.getText().toString().trim();
        String theory = theoryInput.getText().toString().trim();
        String examples = examplesInput.getText().toString().trim();

        if (title.isEmpty() || theory.isEmpty() || examples.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        // проверяем по полю title, чтобы не было дубликатов
        themesRef.get().addOnSuccessListener(snapshot -> {
            boolean exists = false;
            for (DataSnapshot child : snapshot.getChildren()) {
                String existingTitle = child.child("title").getValue(String.class);
                if (existingTitle != null && existingTitle.equalsIgnoreCase(title)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                Toast.makeText(this, "Тема с таким названием уже существует!", Toast.LENGTH_SHORT).show();
                return;
            }

            String tempKey = themesRef.push().getKey();
            final String key = (tempKey != null) ? tempKey : String.valueOf(System.currentTimeMillis());

            ThemeItem theme = new ThemeItem(key, title, theory, examples);

            themesRef.child(key)
                    .setValue(theme)
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(this, AdminTestsActivity.class);
                        intent.putExtra("themeId", key);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Ошибка при проверке тем", Toast.LENGTH_SHORT).show());
    }
}
