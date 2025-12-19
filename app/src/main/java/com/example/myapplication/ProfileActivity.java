package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    // Получение изображения из галереи
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) startCrop(uri);
            });

    // Получение результата UCrop
    private final ActivityResultLauncher<Intent> cropLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    final Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        encodeAndSave(resultUri);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserData();
        loadAvatar();

        // Аватар → выбор изображения
        binding.userAvatar.setOnClickListener(v -> pickImage.launch("image/*"));

        // Удалить аватар
        binding.deleteAvatarBtn.setOnClickListener(v -> deleteAvatar());

        // Стрелка назад
        binding.backArrow.setOnClickListener(v -> finish());

        // Сохранить изменения
        binding.saveBtn.setOnClickListener(v -> saveChanges());

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // Очищаем активити-стек
            Intent i = new Intent(ProfileActivity.this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);
            finish();
        });

    }

    // -------------------- ОБРЕЗКА --------------------
    private void startCrop(@NonNull Uri sourceUri) {
        String destName = UUID.randomUUID().toString() + ".jpg";
        Uri destUri = Uri.fromFile(getCacheDir()).buildUpon().appendPath(destName).build();

        UCrop uCrop = UCrop.of(sourceUri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(600, 600);

        Intent cropIntent = uCrop.getIntent(this);
        cropLauncher.launch(cropIntent);
    }

    // -------------------- СОХРАНЕНИЕ АВАТАРКИ --------------------
    private void encodeAndSave(Uri imgUri) {
        try {
            InputStream stream = getContentResolver().openInputStream(imgUri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            String uid = auth.getCurrentUser().getUid();
            usersRef.child(uid).child("avatar").setValue(base64)
                    .addOnSuccessListener(v ->
                            Toast.makeText(this, "Аватар обновлён!", Toast.LENGTH_SHORT).show()
                    );

            Glide.with(this)
                    .load("data:image/jpeg;base64," + base64)
                    .into(binding.userAvatar);

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
        }
    }

    // -------------------- ЗАГРУЗКА АВАТАРА --------------------
    private void loadAvatar() {
        String uid = auth.getCurrentUser().getUid();

        usersRef.child(uid).child("avatar").get()
                .addOnSuccessListener(snap -> {
                    String base64 = snap.getValue(String.class);

                    if (base64 == null || base64.isEmpty()) {
                        // Показываем стандартную иконку
                        binding.userAvatar.setImageResource(R.drawable.ic_account);
                        return;
                    }

                    Glide.with(this)
                            .load("data:image/jpeg;base64," + base64)
                            .placeholder(R.drawable.ic_account)
                            .into(binding.userAvatar);
                });
    }

    // -------------------- УДАЛЕНИЕ АВАТАРА --------------------
    private void deleteAvatar() {
        String uid = auth.getCurrentUser().getUid();

        usersRef.child(uid).child("avatar").setValue("")
                .addOnSuccessListener(v -> {
                    binding.userAvatar.setImageResource(R.drawable.ic_account);
                    Toast.makeText(this, "Аватар удалён", Toast.LENGTH_SHORT).show();
                });
    }

    // -------------------- ЗАГРУЗКА ТЕКСТОВЫХ ДАННЫХ --------------------
    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();

        usersRef.child(uid).get().addOnSuccessListener(s -> {
            binding.firstnameInput.setText(s.child("firstName").getValue(String.class));
            binding.lastnameInput.setText(s.child("lastName").getValue(String.class));
            binding.emailInput.setText(auth.getCurrentUser().getEmail());
        });
    }

    // -------------------- СОХРАНЕНИЕ ИМЕНИ/ФАМИЛИИ/ПАРОЛЯ --------------------
    private void saveChanges() {
        String uid = auth.getCurrentUser().getUid();

        String fn = binding.firstnameInput.getText().toString().trim();
        String ln = binding.lastnameInput.getText().toString().trim();
        String newPass = binding.passwordInput.getText().toString().trim();

        usersRef.child(uid).child("firstName").setValue(fn);
        usersRef.child(uid).child("lastName").setValue(ln);

        if (!newPass.isEmpty()) {
            auth.getCurrentUser().updatePassword(newPass);
            Toast.makeText(this, "Пароль обновлён", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
    }
}
