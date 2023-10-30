package com.example.ringelman;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    Button btn_cam;
    Button btn_cal;
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
            default:break;
        }
    }
}