package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.*;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private LinearLayout usersContainer;
    private DatabaseReference usersRef;
    private List<UserItem> users = new ArrayList<>();
    private Button addUserButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        // Стрелка назад
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Пользователи");
        }

        mAuth = FirebaseAuth.getInstance();
        usersContainer = findViewById(R.id.adminUsersContainer);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        addUserButton = findViewById(R.id.btnAddUser);

        // Кнопка добавления пользователя
        addUserButton.setOnClickListener(v -> showAddUserDialog());

        loadUsers();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить пользователя");

        // Создаем кастомный layout для диалога
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);

        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);

        // Настройка Spinner для ролей
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (validateUserInput(email, firstName, lastName, password)) {
                createUser(email, password, firstName, lastName, role);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private boolean validateUserInput(String email, String firstName, String lastName, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (firstName.isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (lastName.isEmpty()) {
            Toast.makeText(this, "Введите фамилию", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createUser(String email, String password, String firstName, String lastName, String role) {
        // Создаем пользователя в Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Получаем UID созданного пользователя
                        String uid = task.getResult().getUser().getUid();

                        // Обновляем профиль пользователя (имя)
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(firstName + " " + lastName)
                                .build();

                        task.getResult().getUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        // Сохраняем пользователя в Realtime Database
                                        saveUserToDatabase(uid, email, firstName, lastName, role);

                                        // Показываем пароль администратору
                                        showSuccessDialog(email, password);
                                    } else {
                                        Toast.makeText(this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        String errorMessage = "Ошибка создания пользователя";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String uid, String email, String firstName, String lastName, String role) {
        // Создаем объект User и заполняем поля
        User user = new User();
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.role = role;

        usersRef.child(uid).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Пользователь создан", Toast.LENGTH_SHORT).show();
                    loadUsers(); // Обновляем список
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog(String email, String password) {
        new AlertDialog.Builder(this)
                .setTitle("Пользователь создан")
                .setMessage("Email: " + email + "\nПароль: " + password +
                        "\n\n⚠️ Сохраните пароль! Пользователь должен использовать его для входа.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadUsers() {
        usersRef.get().addOnSuccessListener(snapshot -> {
            usersContainer.removeAllViews();
            users.clear();

            for (DataSnapshot child : snapshot.getChildren()) {
                String uid = child.getKey();
                String firstName = child.child("firstName").getValue(String.class);
                String lastName = child.child("lastName").getValue(String.class);
                String email = child.child("email").getValue(String.class);
                String role = child.child("role").getValue(String.class);

                UserItem item = new UserItem(uid, firstName, lastName, email, role);
                users.add(item);
                addUserView(item);
            }

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show()
        );
    }

    private void addUserView(UserItem user) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_user, usersContainer, false);

        TextView nameTv = row.findViewById(R.id.itemUserName);
        TextView roleTv = row.findViewById(R.id.itemUserRole);
        Button btnView = row.findViewById(R.id.itemUserViewBtn);

        String fullname = (user.lastName != null ? user.lastName + " " : "") +
                (user.firstName != null ? user.firstName : "");

        nameTv.setText(fullname.trim());
        roleTv.setText(user.role != null ? "Роль: " + user.role : "Роль: user");

        btnView.setOnClickListener(v -> showUserDialog(user));

        usersContainer.addView(row);
    }

    private void showUserDialog(UserItem user) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Пользователь");

        StringBuilder sb = new StringBuilder();
        sb.append("ФИО: ")
                .append(user.lastName != null ? user.lastName : "")
                .append(" ")
                .append(user.firstName != null ? user.firstName : "")
                .append("\n");

        sb.append("Email: ").append(user.email != null ? user.email : "—").append("\n");
        sb.append("Роль: ").append(user.role != null ? user.role : "user").append("\n\n");

        sb.append("Пароль скрыт.").append("\n\n");
        sb.append("*Удаление аккаунтов доступно только через Firebase Console.");

        b.setMessage(sb.toString());
        b.setPositiveButton("Закрыть", null);
        b.show();
    }

    private static class UserItem {
        String uid, firstName, lastName, email, role;

        UserItem(String uid, String firstName, String lastName, String email, String role) {
            this.uid = uid;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.role = role;
        }
    }
}