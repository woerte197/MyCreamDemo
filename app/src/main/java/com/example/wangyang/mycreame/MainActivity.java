package com.example.wangyang.mycreame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView camera;
    private TextView storage;
    private final int REQUEST_PERMISSION = 1;
    private final int CAMERA_REQUEST_CODE = 1000;
    private final int CMERA_REQUEST_CHOOSE_CODE = 1001;
    private final int CAMERA_ZOOM_PHOTO = 1002;

    private boolean isHavePermission;
    private static final String TAG = "MainActivity";
    private String takePhotoPath;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = findViewById(R.id.camera);
        storage = findViewById(R.id.storage);
        initPermission();
        initEvent();
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
            }, REQUEST_PERMISSION);
        } else {
            isHavePermission = true;
        }
    }

    private void initEvent() {
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHavePermission)
                    takePhoto();
                else
                    initPermission();
            }
        });
        storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHavePermission)
                    takeKu();
                else
                    initPermission();
            }
        });
    }

    private void takeKu() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CMERA_REQUEST_CHOOSE_CODE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "Photo.jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = MyProvider.getUriForFile(MainActivity.this,
                MainActivity.this.getApplicationContext().getPackageName() + ".my.provider",
                file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult: " + requestCode);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                Log.e(TAG, "onRequestPermissionsResult: " + grantResults);
                for (int permision : grantResults) {
                    if (permision != MainActivity.RESULT_OK) {
                        isHavePermission = false;
                        return;
                    }
                }
                isHavePermission = true;
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Log.e(TAG, "onActivityResult: " + bitmap.toString());
                        startPhotoZoom(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case CMERA_REQUEST_CHOOSE_CODE:
                    try {
                        imageUri = data.getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        startPhotoZoom(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case CAMERA_ZOOM_PHOTO:
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Log.e(TAG, "onActivityResult: " + bitmap.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

    }

    public void startPhotoZoom(Uri uri) {
        Intent intent_gallery_crop = new Intent("com.android.camera.action.CROP");
        intent_gallery_crop.setDataAndType(uri, "image/*");
        intent_gallery_crop.putExtra("crop", "true");
        intent_gallery_crop.putExtra("scale", true);
        intent_gallery_crop.putExtra("aspectX", 1);
        intent_gallery_crop.putExtra("aspectY", 1);
        intent_gallery_crop.putExtra("outputX", 400);
        intent_gallery_crop.putExtra("outputY", 400);
        intent_gallery_crop.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent_gallery_crop.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent_gallery_crop.putExtra("return-data", false);
        startActivityForResult(intent_gallery_crop, CAMERA_ZOOM_PHOTO);
    }
}
