package com.example.myapplication;

public class TestQuestion {
    public String question;
    public String ans1;
    public String ans2;
    public String ans3;
    public String ans4;

    // поле, которое хранит правильный вариант 1..4
    // в базе может быть как "correct" (int), так и "correctOriginal" — оставим оба
    public Integer correct;           // fallback 1..4
    public Integer correctOriginal;   // если есть в старых данных

    public TestQuestion() {} // для Firebase

    public TestQuestion(String question, String ans1, String ans2, String ans3, String ans4, int correct) {
        this.question = question;
        this.ans1 = ans1;
        this.ans2 = ans2;
        this.ans3 = ans3;
        this.ans4 = ans4;
        this.correct = correct;
        this.correctOriginal = correct;
    }

    // возвращает текущее значение correct (1..4) — если есть специальный setCorrect был вызван
    public int getCorrect() {
        if (correct != null) return correct;
        if (correctOriginal != null) return correctOriginal;
        return 1;
    }

    // возвращает текст правильного ответа (после перемешивания ответов)
    public String getCorrectAnswer() {
        try {
            switch (getCorrect()) {
                case 1: return ans1;
                case 2: return ans2;
                case 3: return ans3;
                case 4: return ans4;
                default: return ans1;
            }
        } catch (Exception e) {
            return ans1;
        }
    }

    // установить новый индекс правильного ответа (1..4)
    public void setCorrect(int c) {
        this.correct = c;
    }
}
