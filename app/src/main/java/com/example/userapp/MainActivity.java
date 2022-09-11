package com.example.userapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.userapp.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    private ImageView qrCodeIV;
    String[] medicalRecordArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeIV = findViewById(R.id.idIVQrcode);

        // send http request to get all medical records
        String user = SharedPrefUtils.getUserFromSP(this);
        try {
            fetchAllMedicalRecord(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchAllMedicalRecord(String user) throws IOException {
        String url = "https://e0f8wiau03-e0xgy8n04x-connect.de0-aws-ws.kaleido.io/query";
        String jsonBody = "{\n" +
                "  \"headers\": {\n" +
                "    \"signer\": \"doctor1\",\n" +
                "    \"channel\": \"default-channel\",\n" +
                "    \"chaincode\": \"asset_transfer\"\n" +
                "  },\n" +
                "  \"func\": \"ReadAsset\",\n" +
                "  \"args\": [\n" +
                "    \"" + user + "\"\n" +
                "  ],\n" +
                "  \"strongread\": true\n" +
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

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(myResponse);
                            String medicalRecord = json.getJSONObject("result").getString("MedicalRecord");

                            JSONArray medicalRecordJson = new JSONArray(medicalRecord);
                            ArrayList<String> list = new ArrayList<String>();
                            for (int i = 0; i < medicalRecordJson.length(); i++) {
                                JSONObject jsonObject = new JSONObject(medicalRecordJson.get(i).toString());;
                                String data = "Date: " + jsonObject.getString("date")
                                        + "\nDoctor Name: " + jsonObject.getString("author")
                                        + "\nPrescription: " + jsonObject.getString("prescription");
                                list.add(data);
                            }
                            medicalRecordArray = list.toArray(new String[list.size()]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    public void viewClicked(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_view_records, null);
        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setTitle("Your Medical Record");

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.list_item_medical_record, medicalRecordArray);

        ListView listView = (ListView) promptView.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        Button back = (Button) promptView.findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.cancel();
            }
        });
        alertD.setView(promptView);
        alertD.show();
    }

    public void qrClicked(View view) {
        String user = SharedPrefUtils.getUserFromSP(this);

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = Math.min(width, height);
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(user, null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.getBitmap();
            // the bitmap is set inside our image
            qrCodeIV.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("Tag", e.toString());
        }
    }
}