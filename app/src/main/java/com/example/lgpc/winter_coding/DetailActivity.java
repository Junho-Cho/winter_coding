package com.example.lgpc.winter_coding;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DetailActivity";
    public static Context mDetailContext;

    private ImageView ivLargePhoto;
    private ImageView ivSavePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detail);

        mDetailContext = this;

        ivLargePhoto = (ImageView) findViewById(R.id.iv_showimage_large);
        ivSavePhoto = (ImageView) findViewById(R.id.save_image);

        ivSavePhoto.setOnClickListener(this);

        setImage();
    }

    public void setImage() {
        byte[] bytes = getIntent().getByteArrayExtra("LargePhoto");
        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        ivLargePhoto.setImageBitmap(b);
    }

    @Override
    public void onClick(View view) {
        if (view == ivSavePhoto) {
            byte[] bytes = getIntent().getByteArrayExtra("LargePhoto");
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
            String getTime = sdf.format(date);
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/WinterCoding";
            String file = folder + File.separator + getTime + ".jpg";

            File FolderPath = new File(folder);
            if (!FolderPath.exists()) {
                FolderPath.mkdirs();
                Log.d("MKDIR", folder);
            }

            try {
                FileOutputStream out = new FileOutputStream(file);
                b.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                Toast.makeText(mDetailContext, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
