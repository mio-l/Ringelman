package com.example.ringelman;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

public class ResultActivity extends AppCompatActivity {

    ImageView iv2;
    TextView tv_dat,tv_lvl;
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
    }
    public static Bitmap linGraham(Bitmap srcBitmap){
        int width=srcBitmap.getWidth();
        int height=srcBitmap.getHeight();
        Bitmap dstBitmap=Bitmap.createBitmap(width,height,
                Bitmap.Config.RGB_565);
        int[] pixels=new int[width*height];
        srcBitmap.getPixels(pixels,0,width,0,0,width,height);
        double threshold=getGrayAvg(pixels,pixels.length);
        for(int i=0;i<pixels.length;i++){
            double gray=getGrayLevel(pixels[i]);
            if(gray>threshold) {
                pixels[i] = Color.WHITE;
            }else{
                    pixels[i]=Color.BLACK;
                }
        }
        dstBitmap.setPixels(pixels,0,width,0,0,width,height);
        return dstBitmap;
    }
    public static double ringelmanEmittance(Bitmap srcBitmap){
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
