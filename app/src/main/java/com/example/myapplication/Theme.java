package com.example.myapplication;

public class Theme {

    public String id;
    public String title;
    public String theory;
    public String examples;

    public Theme() { }

    public Theme(String id, String title, String theory, String examples) {
        this.id = id;
        this.title = title;
        this.theory = theory;
        this.examples = examples;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTheory() {
        return theory;
    }

    public String getExamples() {
        return examples;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTheory(String theory) {
        this.theory = theory;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }
}
