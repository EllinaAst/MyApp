package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminEditQuestionActivity extends AppCompatActivity {

    EditText etQuestion, et1, et2, et3, et4, etCorrect;
    Button btnSave;

    DatabaseReference ref;

    private String themeId;      // ← строка
    private String questionId;   // ← ключ вопроса, null если новый

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_edit_question);

        // ===== Toolbar =====
        MaterialToolbar toolbar = findViewById(R.id.adminEditQToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Редактирование вопроса");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        // ====================

        etQuestion = findViewById(R.id.etQuestion);
        et1 = findViewById(R.id.etAns1);
        et2 = findViewById(R.id.etAns2);
        et3 = findViewById(R.id.etAns3);
        et4 = findViewById(R.id.etAns4);
        etCorrect = findViewById(R.id.etCorrect);
        btnSave = findViewById(R.id.btnSaveQuestion);

        themeId = getIntent().getStringExtra("themeId");
        questionId = getIntent().getStringExtra("questionId");

        ref = FirebaseDatabase.getInstance()
                .getReference("tests")
                .child(themeId);

        if (questionId != null) {
            loadQuestion();
        }

        btnSave.setOnClickListener(v -> saveQuestion());
    }

    private void loadQuestion() {
        ref.child(questionId).get().addOnSuccessListener(s -> {
            etQuestion.setText(s.child("question").getValue(String.class));
            et1.setText(s.child("ans1").getValue(String.class));
            et2.setText(s.child("ans2").getValue(String.class));
            et3.setText(s.child("ans3").getValue(String.class));
            et4.setText(s.child("ans4").getValue(String.class));

            Integer c = s.child("correct").getValue(Integer.class);
            etCorrect.setText(c != null ? String.valueOf(c) : "");
        });
    }

    private void saveQuestion() {

        String q = etQuestion.getText().toString().trim();
        String a1 = et1.getText().toString().trim();
        String a2 = et2.getText().toString().trim();
        String a3 = et3.getText().toString().trim();
        String a4 = et4.getText().toString().trim();
        String correctStr = etCorrect.getText().toString().trim();

        if (q.isEmpty() || a1.isEmpty() || a2.isEmpty() || a3.isEmpty() || a4.isEmpty() || correctStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        int correct = Integer.parseInt(correctStr);
        TestQuestion model = new TestQuestion(q, a1, a2, a3, a4, correct);

        if (questionId == null) {
            // новый вопрос
            ref.push().setValue(model).addOnSuccessListener(a -> {
                Toast.makeText(this, "Вопрос добавлен", Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            // обновление
            ref.child(questionId).setValue(model).addOnSuccessListener(a -> {
                Toast.makeText(this, "Вопрос обновлён", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
