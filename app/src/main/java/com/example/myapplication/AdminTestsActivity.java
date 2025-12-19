package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminTestsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private FloatingActionButton btnAdd;
    private DatabaseReference testsRef;
    private String themeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_tests);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.adminTestsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Тесты");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        themeId = getIntent().getStringExtra("themeId");

        testsRef = FirebaseDatabase.getInstance()
                .getReference("tests")
                .child(themeId);

        recycler = findViewById(R.id.recyclerTests);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        btnAdd = findViewById(R.id.btnAddQuestion);
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminEditQuestionActivity.class);
            i.putExtra("themeId", themeId);
            i.putExtra("questionId", (String) null); // создаём новый
            startActivity(i);
        });

        loadQuestions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuestions();
    }

    public void loadQuestions() {
        testsRef.get().addOnSuccessListener(snapshot -> {
            AdminTestsAdapter adapter = new AdminTestsAdapter(snapshot, themeId);
            recycler.setAdapter(adapter);
        });
    }
}
