package com.example.userapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.userapp.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    TextView email;
    TextView password;
    TextView name;
    TextView dob;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        dob = findViewById(R.id.dob);
        progressBar = findViewById(R.id.loading);

        progressBar.setVisibility(View.INVISIBLE);
    }

    public void createAccountClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        String _name = name.getText().toString().replaceAll("\\s","");
        String _dob = dob.getText().toString().replaceAll("\\s","");
        String _contact = email.getText().toString().replaceAll("\\s","");
        String _password = password.getText().toString();

        // send an api request to create a new asset
        // id = _name, name, dob, contact, MedicalRecord = []
        createNewAsset(_name, _dob, _contact);
    }

    private void createNewAsset(String name, String dob, String contact) {
        // send http request
        try {
            String url = "https://e0f8wiau03-e0xgy8n04x-connect.de0-aws-ws.kaleido.io/transactions";
            String jsonBody = "{\n" +
                    "  \"headers\": {\n" +
                    "    \"type\": \"SendTransaction\",\n" +
                    "    \"signer\": \"doctor1\",\n" +
                    "    \"channel\": \"default-channel\",\n" +
                    "    \"chaincode\": \"asset_transfer\"\n" +
                    "  },\n" +
                    "  \"func\": \"CreateAsset\",\n" +
                    "  \"args\": [\n" +
                    "    \"" + name + "\", \"" + name + "\", \"" + dob + "\", \"" + contact + "\", \"[]\"\n" +
                    "  ],\n" +
                    "  \"init\": false\n" +
                    "}";

            OkHttpClient client = new OkHttpClient();

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Basic ZTBwOHlpY2R0ZDpUbTlfYmdNWHR3N2F0ZGF3V0pSVFNKaWtHWEhDNF9TZTM3S28wUFZxaEZZ")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String myResponse = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // save user name in shared pref and send to main activity
                            SharedPrefUtils.saveUserInSP(name, SignUpActivity.this);
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        }
                    });

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginClicked(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}