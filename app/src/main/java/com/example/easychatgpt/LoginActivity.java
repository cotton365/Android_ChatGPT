package com.example.easychatgpt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    static final String PREFS_NAME = "easychatgpt_prefs";
    static final String PREF_BASE_URL = "base_url";
    static final String PREF_API_KEY = "api_key";

    EditText accountEditText;
    EditText passwordEditText;
    EditText baseUrlEditText;
    EditText apiKeyEditText;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountEditText = findViewById(R.id.account_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        baseUrlEditText = findViewById(R.id.base_url_edit_text);
        apiKeyEditText = findViewById(R.id.api_key_edit_text);
        loginButton = findViewById(R.id.login_btn);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        baseUrlEditText.setText(prefs.getString(PREF_BASE_URL, "https://api.openai.com/"));
        apiKeyEditText.setText(prefs.getString(PREF_API_KEY, ""));

        loginButton.setOnClickListener(v -> {
            prefs.edit()
                    .putString(PREF_BASE_URL, baseUrlEditText.getText().toString().trim())
                    .putString(PREF_API_KEY, apiKeyEditText.getText().toString().trim())
                    .apply();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }
}
