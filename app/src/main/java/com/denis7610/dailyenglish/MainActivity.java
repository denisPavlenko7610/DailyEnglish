package com.denis7610.dailyenglish;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView countText;
    private ImageView questionImage;
    private TextView textTranslate;
    private TextView textTranscription;

    private int arrayIndex = 0;

    ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();

        init();

        generateCard();

        showCount();
    }

    private void init() {
        countText = findViewById(R.id.count);
        questionImage = findViewById(R.id.questionImage);
        textTranslate = findViewById(R.id.textTranslate);
        textTranscription = findViewById(R.id.textTranscription);
        Button buttonNo = findViewById(R.id.buttonNo);
        Button buttonYes = findViewById(R.id.buttonYes);
    }

    private void generateQuizes() {
        data.add("ic_001_apple");
        data.add("apple");
        data.add("[æpl]");
        data.add("ic_002_carrot");
        data.add("carrot");
        data.add("[kærət]");
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData();
    }

    private void saveData() {
        if (data.size() != 0) {
            SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(data);
            editor.putString("know list", json);
            editor.apply();
        }
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("know list", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        if (json != null) {
            data = gson.fromJson(json, type);
        } else{
            generateQuizes();
        }
    }

    private void showCount() {
        int count = 0;
        if (data.size() != 0) {
            count = data.size() / 3;
        }
        String countString = "Images: " + count;
        countText.setText(countString);
    }

    private void showNextQuiz() {
        if (arrayIndex >= data.size()) {
            arrayIndex = 0;
        }
        if (data.size() == 0) {
            Toast.makeText(this, "Congratulation! Try Again to improve yourself)", Toast.LENGTH_SHORT).show();
            arrayIndex = 0;
            generateQuizes();
        }
        generateCard();
    }

    private void generateCard() {

        questionImage.setImageResource(getResources().getIdentifier(String.valueOf(data.get(arrayIndex)), "drawable", getPackageName()));

        String text = String.valueOf(data.get(arrayIndex + 1));
        String transcription = String.valueOf(data.get(arrayIndex + 2));

        textTranslate.setText(text);
        textTranscription.setText(transcription);
    }

    public void buttonYes(View view) {
        deleteWord();
        showCount();
        showNextQuiz();
    }

    private void deleteWord() {
        if (data.size()>0) {
            for (int i = 0; i < 3; i++) {
                data.remove(arrayIndex);
            }
        }
    }

    public void buttonNo(View view) {
        showCount();
        arrayIndex += 3;
        showNextQuiz();
    }

    public void showLicense(View view) {
        Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }
}

