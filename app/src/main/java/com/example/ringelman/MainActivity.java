package com.example.ringelman;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btn_cam;
    Button btn_cal;
    Button btn_pic;
    Uri image_uri;
    ImageView iv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        setContentView(R.layout.activity_main);

       iv1=findViewById(R.id.iv1);
        btn_cam=findViewById(R.id.btn_cam);
        btn_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File image_file=new File(getExternalCacheDir(),"temp.jpg");
                if(image_file.exists()){
                    image_file.delete();
                }
                try {
                    image_file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                image_uri= FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.ringelman.image_uri",
                        image_file);
                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
                startActivityForResult(intent,1);
            }
        });

        btn_cal=findViewById(R.id.btn_cal);
        btn_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, ResultActivity.class);
                if(iv1.getDrawable()!=null) {
                    Bitmap bitmap = ((BitmapDrawable) iv1.getDrawable()).getBitmap();
                    Bundle bundle=new Bundle();
                    bundle.putBinder("bitmap",new BitmapBinder(bitmap));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "请选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_pic=findViewById(R.id.btn_alb);
        btn_pic.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // 打开相册应用的 Intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); // 过滤只显示图片文件

                // 使用 startActivityForResult 启动相册选择图片
                startActivityForResult(intent, 2);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    Bitmap bitmap=null;
                    try {
                        bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
                        iv1.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // 获取用户选择的图片的 URI
                        image_uri = data.getData();}}
                Bitmap bitmap=null;
                try {
                    bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                iv1.setImageBitmap(bitmap);
                break;
            default:break;
        }
    }
}