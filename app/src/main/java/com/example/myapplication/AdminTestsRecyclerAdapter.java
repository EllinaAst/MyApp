package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

public class AdminTestsRecyclerAdapter extends RecyclerView.Adapter<AdminTestsRecyclerAdapter.VH> {

    private DataSnapshot snapshot;
    private int themeId;

    public AdminTestsRecyclerAdapter(DataSnapshot snapshot, int themeId) {
        this.snapshot = snapshot;
        this.themeId = themeId;
    }

    @NonNull
    @Override
    public AdminTestsRecyclerAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTestsRecyclerAdapter.VH holder, int position) {
        // получаем N-ый элемент
        int idx = 0;
        DataSnapshot target = null;
        for (DataSnapshot s : snapshot.getChildren()) {
            if (idx == position) {
                target = s;
                break;
            }
            idx++;
        }

        if (target == null) {
            holder.tvTitle.setText("Вопрос " + (position + 1));
            return;
        }

        TestQuestion q = target.getValue(TestQuestion.class);
        holder.tvTitle.setText("Вопрос " + (position + 1));

        String key = target.getKey(); // ключ сообщения — в бд это строка (например "1")

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), AdminEditQuestionActivity.class);
            i.putExtra("themeId", themeId);
            try {
                int id = Integer.parseInt(key);
                i.putExtra("questionId", id);
            } catch (NumberFormatException ex) {
                // если ключ не числовой — передаём -1, в приемнике можно использовать key отдельно
                i.putExtra("questionId", -1);
                Toast.makeText(v.getContext(), "Неподдерживаемый ключ вопроса: " + key, Toast.LENGTH_SHORT).show();
            }
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return (int) snapshot.getChildrenCount();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuestionAdmin);
        }
    }
}
