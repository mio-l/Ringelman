package com.example.ringelman;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class ResultActivity extends AppCompatActivity {

    ImageView iv2;
    TextView tv_dat,tv_lvl;
    Button btn_sav;
    Bitmap bitmap=null;
    static ByteArrayOutputStream byteOut=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        setContentView(R.layout.result);
        Bundle bundle=getIntent().getExtras();
        BitmapBinder bitmapBinder=(BitmapBinder)bundle.getBinder("bitmap");
        Bitmap bitmap=bitmapBinder.getBitmap();
        iv2=findViewById(R.id.iv2);
        iv2.setImageBitmap(bitmap);
        tv_dat=findViewById(R.id.tv_dat);
        tv_lvl=findViewById(R.id.tv_lvl);
        double data=ringelmanEmittance(bitmap);
        BigDecimal d=new BigDecimal(data*100);
        double data1=d.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
        tv_dat.setText(String.valueOf(data1)+"%");
        double level=5*data;
        int level1= (int) Math.round(level);
        tv_lvl.setText(String.valueOf(level1));
        btn_sav=findViewById(R.id.btn_sav);
        btn_sav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap screenshot=
                        takeScreenshot();
                // 保存截图到相册
                MediaStore.Images.Media.insertImage(getContentResolver(), screenshot, "Screenshot", "Screenshot");
                Toast.makeText(ResultActivity.this, "屏幕截图已保存到相册", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public Bitmap takeScreenshot() {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return screenshot;

    }
    public void onPicClick(View view) {
        FileShareUtils.sharePicFile(this);
    }
    public static double ringelmanEmittance(@NonNull Bitmap srcBitmap){
        int width=srcBitmap.getWidth();
        int height=srcBitmap.getHeight();
        Bitmap dstBitmap=Bitmap.createBitmap(width,height,
                Bitmap.Config.RGB_565);
        int[] pixels=new int[width*height];
        srcBitmap.getPixels(pixels,0,width,0,0,width,height);
        double threshold=getGrayAvg(pixels,pixels.length);
        double percent=1-threshold/255;
        return percent;
    }
    public static double getGrayAvg(int[] pixels,int length){
        double sum=0;
        for(int i=0;i<length;i++){
            double gray=getGrayLevel(pixels[i]);
            sum+=gray;
        }
        double grayAvg=sum/length;
        return grayAvg;
    }
    public static double getGrayLevel(int pixel){
        int red= Color.red(pixel);
        int green=Color.green(pixel);
        int blue=Color.blue(pixel);
        return (0.299*red+0.587*green+0.114*blue);
    }
}
