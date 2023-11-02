package com.example.ringelman;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidx.core.content.FileProvider;

public class FileShareUtils {
    private static final String TAG = "FileShareUtils";
    //文件输出流
    private static OutputStream outputStream;

    //创建Bitmap图片
    public static void sharePicFile(Activity context) {
        String fileName = "share";
        File path = getFileUrl(context);
        saveBitmap(context, path, viewToBitmap(context), fileName);
        File file = new File(path + "/" + fileName + ".png");
        startIntent(context, getUriForFile(context, file), fileName);
    }

    //当前界面转化为Bitmap,需要截取状态栏则将stateHeight设置为0
    private static Bitmap viewToBitmap(Activity activity) {
        Bitmap bitmap;
        View view = activity.getWindow().getDecorView();
        //设置是否可以进行绘图缓存
        view.setDrawingCacheEnabled(true);
        //如果绘图缓存无法，强制构建绘图缓存
        view.buildDrawingCache();
        //返回这个缓存视图
        bitmap = view.getDrawingCache();
        //获取状态栏高度（90）
        Rect frame = new Rect();
        //测量屏幕宽和高
        view.getWindowVisibleDisplayFrame(frame);
        int stateHeight = frame.top;
        Display display = activity.getWindowManager().getDefaultDisplay();
       Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        // 根据坐标点和需要的宽和高创建bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, stateHeight, width, height - stateHeight);
        return bitmap;
    }

    //保存图片
    @SuppressLint("SdCardPath")
    public static void saveBitmap(Context context, File dir, Bitmap bitmap, String fileName) {
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, fileName + ".png");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
                bitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送广播更新，扫描某个文件(文件绝对路径，必须是以 Environment.getExternalStorageDirectory() 方法的返回值开头)
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }


    //返回uri
    private static Uri getUriForFile(Context context, File file) {
        //应用包名.fileProvider
        String authority = context.getPackageName().concat(".image_uri");
        Uri fileUri = FileProvider.getUriForFile(context, authority, file);
        return fileUri;
    }

    //返回文件夹
    private static File getFileUrl(Context context) {
        File root = context.getFilesDir();
        File dir = new File(root, "hello/");
        if (!dir.exists()) {
            //创建失败
            if (!dir.mkdir()) {
                Log.e(TAG, "createBitmapPdf: 创建失败");
            }
        }
        return dir;
    }

    //分享文件
    @SuppressLint("WrongConstant")
    private static void startIntent(Context context, Uri fileUri, String fileName) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.putExtra(Intent.EXTRA_STREAM, fileUri);
        share.putExtra(Intent.EXTRA_SUBJECT, fileName);
        String title = "分享标题";
        share.setType("image/*");
        //安卓版本是否大于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.startActivity(Intent.createChooser(share, title));
        } else {
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getFileUrl(context), title)));
            context.startActivity(share);
        }
    }
}
