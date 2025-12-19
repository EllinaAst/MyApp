package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class UserThemesAdapter extends RecyclerView.Adapter<UserThemesAdapter.ViewHolder> {

    private final Context context;
    private final List<ThemeItem> list;

    public UserThemesAdapter(Context ctx, List<ThemeItem> list) {
        this.context = ctx;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_theme, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        ThemeItem t = list.get(pos);

        h.title.setText(t.title != null ? t.title : "Без названия");
        h.percentView.setText("—");

        // LIVE процент успешности
        String uid = FirebaseAuth.getInstance().getCurrentUser() == null
                ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid != null && t.id != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("results")
                    .child(uid)
                    .child(t.firebaseKey)
                    .child("percent");

            // слушаем single -> если хотите live обновления используйте addValueEventListener
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snap) {
                    if (snap.exists()) {
                        Integer percent = snap.getValue(Integer.class);
                        if (percent != null) h.percentView.setText(percent + "%");
                        else h.percentView.setText("—");
                    } else {
                        h.percentView.setText("—");
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    h.percentView.setText("—");
                }
            });
        }

        h.itemView.setOnClickListener(v -> {
            // блокируем неавторизованного
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                new AlertDialog.Builder(context)
                        .setTitle("Авторизация")
                        .setMessage("Чтобы изучать темы, войдите в аккаунт.")
                        .setPositiveButton("Войти", (d, w) ->
                                context.startActivity(new Intent(context, LoginActivity.class)))
                        .setNegativeButton("Отмена", null)
                        .show();
                return;
            }

            // открыть теорию: передаём STRING id темы
            Intent i = new Intent(context, Theme1Activity.class);
            i.putExtra("themeId", t.id);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, percentView;
        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvThemeTitle);
            percentView = v.findViewById(R.id.tvPercent);
        }
    }
}
