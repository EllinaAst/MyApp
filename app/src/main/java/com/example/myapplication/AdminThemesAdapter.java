package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminThemesAdapter extends RecyclerView.Adapter<AdminThemesAdapter.ViewHolder> {

    private Context context;
    private List<Theme> themes;

    public AdminThemesAdapter(Context context, List<Theme> themes) {
        this.context = context;
        this.themes = themes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_theme, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Theme t = themes.get(position);

        holder.title.setText(t.title != null ? t.title : "Без названия");

        // кнопка тестов
        holder.btnTests.setOnClickListener(v -> {
            Intent i = new Intent(context, AdminTestsActivity.class);
            try {
                i.putExtra("themeId", Integer.parseInt(t.id));
                context.startActivity(i);
            } catch (NumberFormatException ex) {
                // если id стринговый — можно показать ошибку
            }
        });

        // кнопка редактировать тему (если есть)
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, AdminEditThemeActivity.class);
            i.putExtra("themeId", t.id);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        Button btnTests;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.themeName);
            btnTests = itemView.findViewById(R.id.btnTests);
        }
    }
}
