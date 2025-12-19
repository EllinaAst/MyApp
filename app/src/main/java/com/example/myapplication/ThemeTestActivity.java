package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThemeTestActivity extends AppCompatActivity {

    private String themeId;
    private int index = 0;
    private int correctCount = 0;
    private final List<TestQuestion> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_test);

        themeId = getIntent().getStringExtra("themeId");

        // Настройка кнопки назад в заголовке
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> showExitConfirmation());

        // Установка текста в заголовке темы
        TextView themeTitle = findViewById(R.id.themeTitle);
        if (themeId != null) {
            FirebaseDatabase.getInstance().getReference("themes")
                    .child(themeId)
                    .child("title")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String title = snapshot.getValue(String.class);
                        if (title != null) {
                            themeTitle.setText("Тест: " + title);
                        }
                    });
        }

        findViewById(R.id.btnNext).setOnClickListener(v -> checkAnswer());
        loadQuestions();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из тестирования")
                .setMessage("Вы уверены, что хотите выйти? Ваш прогресс не будет сохранен, и вам придется начать тест заново.")
                .setPositiveButton("Выйти", (dialog, which) -> {
                    Intent intent = new Intent(ThemeTestActivity.this, ThemeSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Остаться", null)
                .show();
    }

    private void loadQuestions() {
        if (themeId == null) {
            new AlertDialog.Builder(this).setMessage("Не указан themeId").setPositiveButton("OK", null).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("tests")
                .child(themeId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    questions.clear();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        TestQuestion q = s.getValue(TestQuestion.class);
                        if (q != null) {
                            shuffleAnswers(q);
                            questions.add(q);
                        }
                    }
                    Collections.shuffle(questions);
                    index = 0;
                    correctCount = 0;
                    showQuestion();
                })
                .addOnFailureListener(e -> new AlertDialog.Builder(this)
                        .setMessage("Ошибка загрузки: " + e.getMessage()).setPositiveButton("OK", null).show());
    }

    private void showQuestion() {
        if (index >= questions.size()) {
            showResult();
            return;
        }
        TestQuestion q = questions.get(index);

        ((TextView) findViewById(R.id.tvProgress)).setText("Задание " + (index + 1) + " из " + questions.size());
        ((TextView) findViewById(R.id.tvQuestion)).setText(q.question);

        ((RadioButton) findViewById(R.id.rb1)).setText(q.ans1);
        ((RadioButton) findViewById(R.id.rb2)).setText(q.ans2);
        ((RadioButton) findViewById(R.id.rb3)).setText(q.ans3);
        ((RadioButton) findViewById(R.id.rb4)).setText(q.ans4);

        ((RadioGroup) findViewById(R.id.rgAnswers)).clearCheck();

        Button btn = findViewById(R.id.btnNext);
        btn.setText(index == questions.size() - 1 ? "Завершить тест" : "Следующий вопрос");
    }

    private void checkAnswer() {
        RadioGroup rg = findViewById(R.id.rgAnswers);
        int id = rg.getCheckedRadioButtonId();
        if (id == -1) return;

        RadioButton rb = findViewById(id);
        String chosen = rb.getText().toString();

        TestQuestion q = questions.get(index);
        String correctAnswer = q.getCorrectAnswer();

        if (chosen.equals(correctAnswer)) correctCount++;

        index++;
        showQuestion();
    }

    private void showResult() {
        int percent = questions.size() == 0 ? 0 : (int) (((double) correctCount / questions.size()) * 100);
        String uid = FirebaseAuth.getInstance().getCurrentUser() == null ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("results")
                    .child(uid)
                    .child(themeId)
                    .child("percent")
                    .setValue(percent);
        }

        new AlertDialog.Builder(this)
                .setTitle("Результат тестирования")
                .setMessage("Правильных ответов: " + correctCount + " из " + questions.size() + "\nУспешность: " + percent + "%")
                .setCancelable(false)
                .setPositiveButton("Вернуться к темам", (d, w) -> {
                    Intent back = new Intent(ThemeTestActivity.this, ThemeSelectionActivity.class);
                    back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(back);
                    finish();
                })
                .show();
    }

    private void shuffleAnswers(TestQuestion q) {
        List<String> answers = new ArrayList<>();
        answers.add(q.ans1);
        answers.add(q.ans2);
        answers.add(q.ans3);
        answers.add(q.ans4);

        int origIndex = 1;
        try {
            if (q.correctOriginal != null) origIndex = q.correctOriginal;
            else if (q.correct != null) origIndex = q.correct;
        } catch (Exception ignored) {}

        String correctAnswer = answers.get(Math.max(0, Math.min(3, origIndex - 1)));

        Collections.shuffle(answers);

        q.ans1 = answers.get(0);
        q.ans2 = answers.get(1);
        q.ans3 = answers.get(2);
        q.ans4 = answers.get(3);

        for (int i = 0; i < 4; i++) {
            if (answers.get(i) != null && answers.get(i).equals(correctAnswer)) {
                q.setCorrect(i + 1);
                break;
            }
        }
    }
}