package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminThemesActivity extends AppCompatActivity {

    private DatabaseReference themesRef;
    private LinearLayout themesContainer;
    private EditText searchEditText;
    private List<ThemeItem> allThemes = new ArrayList<>();
    private ValueEventListener themesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_themes);

        // Инициализация элементов
        Toolbar toolbar = findViewById(R.id.adminThemesToolbar);
        setSupportActionBar(toolbar);

        // Убираем заголовок по умолчанию
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        searchEditText = findViewById(R.id.searchEditText);
        themesContainer = findViewById(R.id.themesContainer);

        Button addThemeButton = findViewById(R.id.btn_add_theme);
        addThemeButton.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCreateThemeActivity.class))
        );

        themesRef = FirebaseDatabase.getInstance().getReference("themes");

        // Настройка поиска
        setupSearch();

        // Загрузка тем
        loadThemes();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterThemes(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadThemes() {
        if (themesListener != null) {
            themesRef.removeEventListener(themesListener);
        }

        themesListener = themesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allThemes.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    ThemeItem theme = new ThemeItem();
                    theme.firebaseKey = child.getKey();
                    theme.id = child.getKey();
                    theme.title = child.child("title").getValue(String.class);
                    theme.theory = child.child("theory").getValue(String.class);
                    theme.examples = child.child("examples").getValue(String.class);

                    allThemes.add(theme);
                }

                // Показываем все темы при первой загрузке
                filterThemes(searchEditText.getText().toString().trim().toLowerCase());
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(AdminThemesActivity.this, "Ошибка загрузки тем", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterThemes(String query) {
        themesContainer.removeAllViews();

        List<ThemeItem> filteredThemes = new ArrayList<>();

        if (query.isEmpty()) {
            filteredThemes.addAll(allThemes);
        } else {
            for (ThemeItem theme : allThemes) {
                if (theme.title != null && theme.title.toLowerCase().contains(query)) {
                    filteredThemes.add(theme);
                }
            }
        }

        // Показать сообщение, если ничего не найдено
        if (filteredThemes.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Темы не найдены");
            emptyView.setTextSize(16);
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            emptyView.setPadding(0, 32, 0, 0);
            emptyView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            themesContainer.addView(emptyView);
            return;
        }

        // Отображаем отфильтрованные темы
        for (ThemeItem theme : filteredThemes) {
            View item = getLayoutInflater().inflate(R.layout.item_admin_theme, themesContainer, false);

            TextView titleTxt = item.findViewById(R.id.themeName);
            titleTxt.setText(theme.title != null ? theme.title : "Без названия");

            // Кнопка редактирования
            item.findViewById(R.id.btnEditTheme).setOnClickListener(v -> {
                Intent i = new Intent(AdminThemesActivity.this, AdminEditThemeActivity.class);
                i.putExtra("themeId", theme.firebaseKey);
                startActivity(i);
            });

            // Кнопка тестов
            item.findViewById(R.id.btnTests).setOnClickListener(v -> {
                Intent i = new Intent(AdminThemesActivity.this, AdminTestsActivity.class);
                i.putExtra("themeId", theme.firebaseKey);
                startActivity(i);
            });

            // Кнопка удаления
            item.findViewById(R.id.btnDeleteTheme).setOnClickListener(v -> {
                new AlertDialog.Builder(AdminThemesActivity.this)
                        .setTitle("Удалить тему?")
                        .setMessage("При удалении темы будут также удалены все связанные тесты. Продолжить?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            if (theme.firebaseKey != null) {
                                themesRef.child(theme.firebaseKey).removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            FirebaseDatabase.getInstance()
                                                    .getReference("tests")
                                                    .child(theme.firebaseKey)
                                                    .removeValue();
                                            Toast.makeText(AdminThemesActivity.this,
                                                    "Тема и тесты удалены", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(AdminThemesActivity.this,
                                                "Ошибка удаления", Toast.LENGTH_SHORT).show());
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });

            themesContainer.addView(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (themesListener != null) {
            themesRef.removeEventListener(themesListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран обновляем поиск
        filterThemes(searchEditText.getText().toString().trim().toLowerCase());
    }
}