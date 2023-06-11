# Simunaden
植物拍照识别，虚拟植物养成游戏

[![Platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gradle Version](https://img.shields.io/badge/gradle-7.0-green.svg)](https://docs.gradle.org/current/release-notes)
[![Awesome Badge](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)](https://java-lang.github.io/awesome-java)

## 项目实现的功能
1. 原有UI组件的中文化

2. UI组件增加事件监听

3. 花园页面可以与植物交互，播放动画

4. 实现从图库选择图片或者拍照

5. 使用Retrofit实现对植物识别API的调用

6. 使用植物识别API对图片进行识别, 显示识别结果（可能因为网络问题，目前对于申请的100次调用仅使用了一次）

## 技术难点
### Retrofit:
Retrofit用于发起网络请求，项目中使用它调用植物识别API。

1. 创建Retrofit对象，传入Base URL

2. 创建接口，定义需要请求的API

3. 创建接口的实现类，调用接口方法发起请求

4. 在回调方法中处理响应结果

```java
Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
plantApiService = retrofit.create(PlantApiService.class);

// 定义接口
private interface PlantApiService {
   @Headers({"Api-Key: " + API_KEY, "Content-Type: application/json"})
   @POST("identify")
   Call<JsonObject> identifyPlant(@Body JsonObject requestBody);
}

// 发起请求
Call<JsonObject> call = plantApiService.identifyPlant(jsonRequest);
call.enqueue(new Callback<JsonObject>() {
  // 处理响应    
  @Override
  public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
     if (response.isSuccessful()) {
       // 解析JSON结果
     } else {
       // 请求失败处理
     }
  }
}); 
```

### 权限申请:
项目需要读写外部存储权限来选择图片，需要进行权限申请。

1. 检查是否有权限，如果没有就进行申请

2. 申请权限时，需要在请求码中传入一个请求码REQUEST_CODE，然后重写onRequestPermissionsResult方法

3. 在onRequestPermissionsResult中根据请求码判断是哪个权限的申请，如果授权就进行后续操作，否则提示用户授权

```java
if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
   requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
} else {
   // 有权限,执行选择图片操作
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
   if (requestCode == REQUEST_CODE) {
       if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           // 权限授权,执行选择图片操作
       } else {
           Toast.makeText(requireContext(), "需要存储权限来选择图片", Toast.LENGTH_SHORT).show();
       } 
   }
}
```

### 图片选择:
项目中选择图片的方法是启动一个Intent，选择图片后会调用onActivityResult方法。

1. 创建Intent，设置Action为Intent.ACTION_PICK,Type为image/*

2. 启动Activity，传入一个请求码REQUEST_CODE_PICK

3. 在onActivityResult中根据请求码判断是否是图片选择的回调，如果是则可以从Intent中获取选中的图片URI

4. 通过URI加载Bitmap

```java
Intent intent = new Intent(Intent.ACTION_PICK);
intent.setType("image/*");
startActivityForResult(intent, REQUEST_CODE_PICK);

@Override
public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
   if (requestCode == REQUEST_CODE_PICK) {
       if (resultCode == RESULT_OK) {
           Uri imageUri = data.getData();
           try {
               Bitmap imageBitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(imageUri));
           } catch (FileNotFoundException e) {
               Log.e("PlantApp", "图片文件不存在");
           }
       } 
   }
}
```

### Fragment:
项目中使用Fragment来实现不同页面。

1. 继承Fragment，实现onCreateView等方法

2. 将Fragment添加到Activity的FragmentManager中进行管理

3. 可以通过getActivity()获取宿主Activity的上下文

4. 可以在不同Fragment之间传递参数

```java
public class HomeFragment extends Fragment {
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
     // 返回Fragment的布局
  }
  
  // 添加Fragment
  fragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
               .commit();
  
  // 获取Context
  Context context = getActivity();
  
  // 传参
  Bundle bundle = new Bundle();
  bundle.putString("key", "value");
  fragment.setArguments(bundle);
  String value = fragment.getArguments().getString("key");  
}
```

### 动画和交互:
项目的花园页面实现了浇水动画。

1. 在XML中定义LottieAnimationView来加载动画资源

2. 在代码中获取LottieAnimationView，并调用playAnimation()等方法控制动画

3. 可以设置每次播放动画的起始帧，实现动画的累加效果

```xml
<com.airbnb.lottie.LottieAnimationView 
     android:id="@+id/animation_view"
     android:layout_width="wrap_content"
     android:layout_height="wrap_content"
     android:src="@raw/watering_anim" />
``` 
```java
animationView = view.findViewById(R.id.animation_view);
animationView.setAnimation(R.raw.watering_anim);

if (count > max_count) {
  animationView.playAnimation(); 
} else {
  animationView.setFrame((count * 50));  // 设置起始帧
  count++;
}
```

# Source
Repo to demonstrate Plant App UI in Android app. This is a follow up on the source at :

- [Plant App Flutter UI](https://www.youtube.com/watch?v=LN668OAUrK4&feature=youtu.be)
- [AndroidMultipleViewRecyclerView](https://github.com/uigitdev/AndroidMultipleViewRecyclerView)
- [Flat Icon](https://www.flaticon.com)

# License

```
    Copyright (C) Achmad Qomarudin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```
