package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {

    String sImage;
    String parsedText;
    Button getTextBtn;

    TextView parsedView;

    Button gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gallery = findViewById(R.id.button);
        getTextBtn = findViewById(R.id.button2);
        getTextBtn.setVisibility(View.INVISIBLE);
        parsedView = findViewById(R.id.textView);

        gallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,3);
            }
        });

        getTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeApiCall();
            }
        });

    }

    private void makeApiCall() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("language","eng")
                .addFormDataPart("isOverlayRequired","false")
                .addFormDataPart("iscreatesearchablepdf","false")
                .addFormDataPart("issearchablepdfhidetextlayer","false")
                .addFormDataPart("filetype","jpg")
                .addFormDataPart("base64Image",sImage)
                .build();
        Request request = new Request.Builder()
                .url("https://api.ocr.space/parse/image")
                .method("POST", body)
                .addHeader("apikey", "K89101414388957")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    try {
                        JSONObject json = new JSONObject(myResponse);
                        JSONArray Jarray = json.getJSONArray("ParsedResults");
                        JSONObject object     = Jarray.getJSONObject(0);
                        parsedText = object.getString("ParsedText");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parsedView.setText(parsedText);
                            // Update your UI with the response
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK && data!=null){
            Uri selectedImage = data.getData();
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageURI(selectedImage);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                // initialize byte stream
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // compress Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                // Initialize byte array
                byte[] bytes = stream.toByteArray();
                // get base64 encoded string
                sImage = "data:image/jpeg;base64,"+Base64.encodeToString(bytes, Base64.DEFAULT);

                getTextBtn.setVisibility(View.VISIBLE);

                System.out.println(sImage.substring(0,50));

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}