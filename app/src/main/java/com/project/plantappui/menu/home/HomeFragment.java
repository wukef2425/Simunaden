package com.project.plantappui.menu.home;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.project.plantappui.R;
import com.project.plantappui.adapter.GroupAdapter;
import com.project.plantappui.api.PlantIdApi;
import com.project.plantappui.model.Group;
import com.project.plantappui.model.Plant;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private Context mContext;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private GroupAdapter groupAdapter;
    private ArrayList<Group> groups;
    private ArrayList<Plant> featured_plants;
    private ArrayList<Plant> recommended;

    // 植物识别接口要用到的
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 100;
    private PlantIdApi plantIdApi;
    private File photoFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plantIdApi = new PlantIdApi();

        de.hdodenhof.circleimageview.CircleImageView imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                } else {
                    pickImageFromGallery();
                }
            }
        });

        setAdapterType(view);
        setAdapter();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(requireContext(), "Storage permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("PlantApp", "onActivityResult() called");
        Log.d("PlantApp", "Request code: " + requestCode);
        Log.d("PlantApp", "Result code: " + resultCode);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("PlantApp", "Image captured successfully");
            if (photoFile != null) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                identifyPlant(imageBitmap);
            }
        } else if (requestCode == REQUEST_IMAGE_PICK) {
            if (resultCode == RESULT_OK) {
                Log.d("PlantApp", "Image picked successfully");
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap imageBitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(imageUri));
                        identifyPlant(imageBitmap);
                    } catch (FileNotFoundException e) {
                        Log.e("PlantApp", "Image file not found", e);
                    }
                }
            } else {
                Log.d("PlantApp", "Image pick cancelled");
            }
        } else {
            Log.d("PlantApp", "Unexpected result");
        }
    }

    private void identifyPlant(Bitmap imageBitmap) {
        plantIdApi.identifyPlant(imageBitmap, new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseData = response.body().getAsJsonObject();
                    // 解析并处理 API 响应数据
                    JsonArray suggestionsArray = responseData.getAsJsonArray("suggestions");
                    List<String> plantNames = new ArrayList<>();
                    List<List<String>> commonNamesList = new ArrayList<>();
                    List<String> urls = new ArrayList<>();

                    for (JsonElement suggestionElement : suggestionsArray) {
                        JsonObject suggestionObject = suggestionElement.getAsJsonObject();
                        String plantName = suggestionObject.get("plant_name").getAsString();
                        JsonObject plantDetailsObject = suggestionObject.getAsJsonObject("plant_details");
                        JsonArray commonNamesArray = plantDetailsObject.getAsJsonArray("common_names");
                        String url = plantDetailsObject.get("url").getAsString();

                        List<String> commonNames = new ArrayList<>();
                        for (JsonElement commonNameElement : commonNamesArray) {
                            commonNames.add(commonNameElement.getAsString());
                        }

                        plantNames.add(plantName);
                        commonNamesList.add(commonNames);
                        urls.add(url);
                    }
                    showPlantResults(plantNames, commonNamesList, urls);
                } else {
                    Log.e("PlantApp", "API response error: " + response.message());
                }
            }

            private void showPlantResults(List<String> plantNames, List<List<String>> commonNamesList, List<String> urls) {
                StringBuilder resultsMessage = new StringBuilder();

                for (int i = 0; i < plantNames.size(); i++) {
                    resultsMessage.append(plantNames.get(i)).append("\n");
                    resultsMessage.append("Common names: ").append(TextUtils.join(", ", commonNamesList.get(i))).append("\n");
                    resultsMessage.append("URL: ").append(urls.get(i)).append("\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Plant Identification Results");
                builder.setMessage(resultsMessage.toString());
                builder.setPositiveButton("OK", null);
                builder.show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("PlantApp", "API call failure: " + t.getMessage());
            }
        });
    }

    private void initGroupData() {
        groups = new ArrayList<>();
        groups.add(new Group("为你推荐", "更多"));
        groups.add(new Group("最近上新", "更多"));
    }

    private void initPlantData() {
        featured_plants = new ArrayList<>();
        recommended     = new ArrayList<>();

        featured_plants.add(new Plant("Colorado Columbines", "Indonesia", "$300", R.drawable.bottom_img_1));
        featured_plants.add(new Plant("Common Mallows", "Russia", "$200", R.drawable.bottom_img_1));
        featured_plants.add(new Plant("Cherry Blossom", "Italy", "$100", R.drawable.bottom_img_1));

        recommended.add(new Plant("Aquilegia", "Indonesia", "$600", R.drawable.image_1));
        recommended.add(new Plant("Angelica", "Russia", "$500", R.drawable.image_2));
        recommended.add(new Plant("Camellia", "Italy", "$400", R.drawable.image_3));
        recommended.add(new Plant("Narcissa", "France", "$300", R.drawable.image_1));
        recommended.add(new Plant("Orchid", "China", "$200", R.drawable.image_2));
        recommended.add(new Plant("Lily", "America", "$100", R.drawable.image_3));
    }

    private void setAdapterType(View view) {
        recyclerView    = view.findViewById(R.id.recyclerView);
        layoutManager   = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        initGroupData();
        initPlantData();
        //todo 1. Add the new object to the parameter list.
        groupAdapter = new GroupAdapter(mContext, groups, featured_plants, recommended);
        recyclerView.setAdapter(groupAdapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}