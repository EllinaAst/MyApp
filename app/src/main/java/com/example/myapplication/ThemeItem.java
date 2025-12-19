package com.example.myapplication;

public class ThemeItem {

    public String id;
    public String title;
    public String theory;
    public String examples;

    // 🔥 Добавляем поле ключа Firebase
    public String firebaseKey;

    public ThemeItem() {}

    public ThemeItem(String id, String title, String theory, String examples) {
        this.id = id;
        this.title = title;
        this.theory = theory;
        this.examples = examples;
    }
}
