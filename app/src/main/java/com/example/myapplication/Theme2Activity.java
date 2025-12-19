package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Theme2Activity extends AppCompatActivity {

    private TextView themeTitle, userInfo;
    private Button backButton, nextButton;
    private LinearLayout examplesContainer;
    private String themeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme2);

        themeTitle = findViewById(R.id.themeTitle);
        userInfo = findViewById(R.id.userInfo);
        backButton = findViewById(R.id.backButton);
        examplesContainer = findViewById(R.id.examplesContainer);
        nextButton = findViewById(R.id.nextButton);

        themeId = getIntent().getStringExtra("themeId");

        loadUserInfo();
        loadExamples();

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                showAuthDialog();
                return;
            }
            Intent i = new Intent(Theme2Activity.this, ThemeTestActivity.class);
            i.putExtra("themeId", themeId);
            startActivity(i);
        });
    }

    private void showAuthDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Авторизация")
                .setMessage("Чтобы проходить тему, войдите в аккаунт.")
                .setPositiveButton("Войти", (d, w) -> startActivity(new Intent(this, LoginActivity.class)))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void loadUserInfo() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            userInfo.setText("Гость");
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnSuccessListener(s -> {
                    String fn = s.child("firstName").getValue(String.class);
                    String ln = s.child("lastName").getValue(String.class);
                    userInfo.setText((ln + " " + fn).trim());
                });
    }

    private void loadExamples() {
        if (themeId == null) return;
        FirebaseDatabase.getInstance().getReference("themes")
                .child(themeId)
                .get()
                .addOnSuccessListener(s -> {
                    themeTitle.setText(s.child("title").getValue(String.class));
                    String examples = s.child("examples").getValue(String.class);
                    examplesContainer.removeAllViews();
                    if (examples == null || examples.isEmpty()) {
                        TextView tv = new TextView(this);
                        tv.setText("Примеры отсутствуют.");
                        tv.setTextSize(16);
                        tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                        examplesContainer.addView(tv);
                        return;
                    }
                    for (String line : examples.split("\n")) {
                        TextView tv = new TextView(this);
                        tv.setText(line);
                        tv.setTextSize(16);
                        tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                        tv.setPadding(0, 8, 0, 8);
                        examplesContainer.addView(tv);
                    }
                });
    }
}