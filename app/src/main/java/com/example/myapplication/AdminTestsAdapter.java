package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminTestsAdapter extends RecyclerView.Adapter<AdminTestsAdapter.ViewHolder> {

    private List<DataSnapshot> list = new ArrayList<>();
    private String themeId;

    public AdminTestsAdapter(DataSnapshot snapshot, String themeId) {
        this.themeId = themeId;
        if (snapshot != null) {
            for (DataSnapshot child : snapshot.getChildren()) {
                list.add(child);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DataSnapshot s = list.get(position);
        String title = s.child("question").getValue(String.class);
        if (title == null) title = "Вопрос " + (position + 1);

        holder.tvTitle.setText(title);
        String key = s.getKey();

        // Открыть вопрос
        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, AdminEditQuestionActivity.class);
            i.putExtra("themeId", themeId);
            i.putExtra("questionId", key);
            ctx.startActivity(i);
        });

        // Удалить вопрос — длинное нажатие
        holder.itemView.setOnLongClickListener(v -> {

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Удалить вопрос?")
                    .setMessage("Это действие нельзя отменить.")
                    .setPositiveButton("Удалить", (dialog, which) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("tests")
                                .child(themeId)
                                .child(key)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {

                                    Toast.makeText(v.getContext(), "Вопрос удалён", Toast.LENGTH_SHORT).show();

                                    // обновление списка
                                    list.remove(position);
                                    notifyItemRemoved(position);
                                });
                    })
                    .setNegativeButton("Отмена", null)
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuestionAdmin);
        }
    }
}
