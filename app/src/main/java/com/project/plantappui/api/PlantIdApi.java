package com.project.plantappui.api;

import android.graphics.Bitmap;
import android.util.Base64;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class PlantIdApi {
    private static final String BASE_URL = "https://plant.id/v2/";
    private static final String API_KEY = "RjJloBWoV7VFqQIy0N5ha1LPRnbY8ivdWr90KNLNr9WngNskO1";

    private PlantApiService plantApiService;

    public PlantIdApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        plantApiService = retrofit.create(PlantApiService.class);
    }

    public void identifyPlant(Bitmap imageBitmap, Callback<JsonObject> callback) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        JsonObject jsonRequest = new JsonObject();

        JsonArray organsArray = new JsonArray();
        organsArray.add("leaf");
        jsonRequest.add("organs", organsArray);

        jsonRequest.addProperty("organs_quality", "good");
        jsonRequest.addProperty("organs_sample_size", 1);

        JsonArray imagesArray = new JsonArray();
        imagesArray.add(encodedImage);
        jsonRequest.add("images", imagesArray);

        Call<JsonObject> call = plantApiService.identifyPlant(jsonRequest);
        call.enqueue(callback);
    }

    private interface PlantApiService {
        @Headers({"Api-Key: " + API_KEY, "Content-Type: application/json"})
        @POST("identify")
        Call<JsonObject> identifyPlant(@Body JsonObject requestBody);
    }
}