package com.project.plantappui.menu.garden;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.project.plantappui.R;

public class GardenFragment extends Fragment {
    private LottieAnimationView animationView;
    private Button waterBtn;
    private int count = 0;
    private final int max_count = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

        // 恢复count的值
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt("count");
        }

        View view = inflater.inflate(R.layout.fragment_garden, container, false);

        animationView = view.findViewById(R.id.animation_view);
        animationView.setAnimation(R.raw.watering_anim);

        waterBtn = view.findViewById(R.id.water_btn);
        waterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count > max_count) {
                    waterBtn.setText("植物已成熟");
                    animationView.playAnimation();
                    return;
                }
                animationView.setFrame((count * 50));
                count++;
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count", count);
    }
}