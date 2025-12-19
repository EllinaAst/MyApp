package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Theme1Activity extends AppCompatActivity {

    private TextView themeTitle, theoryText, userInfo;
    private Button backButton, nextButton;
    private String themeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme1);

        themeTitle = findViewById(R.id.themeTitle);
        theoryText = findViewById(R.id.theoryText);
        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);
        userInfo = findViewById(R.id.userInfo);

        themeId = getIntent().getStringExtra("themeId");

        loadTheory();
        loadUserInfo();

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(v -> {
            Intent i = new Intent(Theme1Activity.this, Theme2Activity.class);
            i.putExtra("themeId", themeId);
            startActivity(i);
        });
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

    private void loadTheory() {
        if (themeId == null) return;
        FirebaseDatabase.getInstance().getReference("themes")
                .child(themeId)
                .get()
                .addOnSuccessListener(s -> {
                    themeTitle.setText(s.child("title").getValue(String.class));
                    theoryText.setText(s.child("theory").getValue(String.class));
                });
    }
}