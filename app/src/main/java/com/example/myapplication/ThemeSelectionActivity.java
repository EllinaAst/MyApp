package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ThemeSelectionActivity extends AppCompatActivity {

    private TextView userInfo;
    private ImageView profileIcon;
    private RecyclerView recycler;
    private UserThemesAdapter adapter;
    private List<ThemeItem> themes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_selection);

        userInfo = findViewById(R.id.userInfo);
        profileIcon = findViewById(R.id.userAvatarTheme);
        recycler = findViewById(R.id.recyclerThemes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Открытие профиля
        profileIcon.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                new AlertDialog.Builder(this)
                        .setTitle("Вы не авторизованы")
                        .setMessage("Чтобы просматривать профиль — войдите в аккаунт.")
                        .setPositiveButton("Войти", (d, w) ->
                                startActivity(new Intent(this, LoginActivity.class)))
                        .setNegativeButton("Отмена", null)
                        .show();
                return;
            }
            startActivity(new Intent(this, ProfileActivity.class));
        });

        checkAdminBlock();
        loadUserInfo();
        loadUserAvatar();
        loadThemes();
    }

    // ============= Проверка роли админа =============
    private void checkAdminBlock() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return; // гость

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    String role = snapshot.child("role").getValue(String.class);

                    if ("admin".equals(role)) {
                        startActivity(new Intent(this, AdminPanelActivity.class));
                        finish();
                    }
                });
    }

    // ============= Загрузка имени пользователя =============
    private void loadUserInfo() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            userInfo.setText("Гость");
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener(s -> {
                    String fn = s.child("firstName").getValue(String.class);
                    String ln = s.child("lastName").getValue(String.class);
                    userInfo.setText((ln + " " + fn).trim());
                });
    }

    // ============= Загрузка аватара пользователя =============
    private void loadUserAvatar() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            profileIcon.setImageResource(R.drawable.ic_account);
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("avatar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            profileIcon.setImageResource(R.drawable.ic_account);
                            return;
                        }

                        String base64 = snapshot.getValue(String.class);

                        if (base64 == null || base64.trim().isEmpty()) {
                            profileIcon.setImageResource(R.drawable.ic_account);
                            return;
                        }

                        String uri = "data:image/jpeg;base64," + base64;

                        Glide.with(ThemeSelectionActivity.this)
                                .load(uri)
                                .placeholder(R.drawable.ic_account)
                                .error(R.drawable.ic_account)
                                .into(profileIcon);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ============= Загрузка списка тем =============
    private void loadThemes() {

        FirebaseDatabase.getInstance().getReference("themes")
                .get()
                .addOnSuccessListener(snapshot -> {

                    themes.clear();

                    for (DataSnapshot child : snapshot.getChildren()) {

                        ThemeItem item = child.getValue(ThemeItem.class);
                        if (item == null) continue;

                        item.firebaseKey = child.getKey(); // ← ВАЖНО!

                        themes.add(item);
                    }

                    UserThemesAdapter adapter = new UserThemesAdapter(this, themes);
                    recycler.setAdapter(adapter);
                });
    }

}
