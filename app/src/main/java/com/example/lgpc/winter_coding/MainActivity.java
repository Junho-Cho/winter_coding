package com.example.lgpc.winter_coding;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int LOAD_SUCCESS = 101;
    public static Context mContext;

    private String SEARCH_URL = "https://secure.flickr.com/services/rest/?method=flickr.photos.search";
    private String API_KEY = "&api_key=ddfc63d568aa90ee54f3f99a674984b4";
    private String PER_PAGE = "&per_page=20";
    private String SORT = "&sort=interestingness-desc";
    private String FORMAT = "&format=json";
    private String CONTECT_TYPE = "&content_type=1";
    private String SEARCH_TEXT = "&text=";
    private String REQUEST_URL = SEARCH_URL + API_KEY + PER_PAGE + SORT + FORMAT + CONTECT_TYPE + SEARCH_TEXT;

    private EditText searchKeyword = null;
    private ImageView ivCallJSON;
    private RecyclerView lvPhotoList;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter myAdapter = null;
    private ArrayList<PhotoInfo> photoInfoArrayList = null;

    private ProgressDialog progressDialog = null;
    private int cnt = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mContext = this;

        ivCallJSON = (ImageView) findViewById(R.id.iv_main_requestjson);
        lvPhotoList = findViewById(R.id.lv_main_list);
        lvPhotoList.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        lvPhotoList.setLayoutManager(mLayoutManager);
        searchKeyword = (EditText)findViewById(R.id.et_main_searchkeyword);

        photoInfoArrayList = new ArrayList<>();

        myAdapter = new MyAdapter(photoInfoArrayList);
        lvPhotoList.setAdapter(myAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("이미지 로딩중입니다...");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("알림");
        alertDialogBuilder.setMessage("이미지 로딩의 속도를 높이기 위해 모바일 데이터 환경을 추천드립니다.");
        alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();

        ivCallJSON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchKeyword.getText().toString().length() != 0) {
                    progressDialog.show();
                } else {
                    Toast.makeText(MainActivity.this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }

                String keyword = searchKeyword.getText().toString();
                getJSON(keyword);
            }
        });
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainactivity) {
            weakReference = new WeakReference<MainActivity>(mainactivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainactivity = weakReference.get();

            if (mainactivity != null) {
                switch (msg.what) {
                    case LOAD_SUCCESS:
                        mainactivity.myAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    public void getJSON(final String keyword) {
        if (keyword == null) return;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result;
                try {
                    Log.d(TAG, REQUEST_URL+keyword);
                    URL url = new URL(REQUEST_URL+keyword);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.connect();

                    int responseStatusCode = httpURLConnection.getResponseCode();
                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();
                    result = sb.toString().trim();
                } catch (Exception e) {
                    result = e.toString();
                }

                if (jsonParser(result)) {
                    Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
                    mHandler.sendMessage(message);
                }
            }
        });
        thread.start();
    }

    private class showimageTask extends AsyncTask<String, Void, Bitmap[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        };

        @Override
        protected Bitmap[] doInBackground(String... url) {
            String thumbnailPhotoURL = url[0].toString();
            String largePhotoURL = url[1].toString();

            Bitmap[] bitmap = new Bitmap[2];
            bitmap[0] = getImagefromURL(thumbnailPhotoURL);
            bitmap[1] = getImagefromURL(largePhotoURL);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmap) {
            super.onPostExecute(bitmap);

            photoInfoArrayList.add(new PhotoInfo(bitmap[0], bitmap[1]));
            myAdapter.notifyDataSetChanged();

            double progress = 5 * cnt;
            progressDialog.setMessage("Downloded " + ((int) progress) + "% ...");
            cnt += 1;
            if ((int)progress >= 100) {
                progressDialog.dismiss();
            }
        }
    }

    public Bitmap getImagefromURL(final String photoURL) {
        if (photoURL == null) return null;

        try {
            URL url = new URL(photoURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setReadTimeout(3000);
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();

            int responseStatusCode = httpURLConnection.getResponseCode();

            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            } else return null;

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

            bufferedInputStream.close();
            httpURLConnection.disconnect();

            return bitmap;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return null;
    }

    public boolean jsonParser(String jsonString) {
        if (jsonString == null) return false;

        jsonString = jsonString.replace("jsonFlickrApi(", "");
        jsonString = jsonString.replace(")", "");

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject photos = jsonObject.getJSONObject("photos");
            JSONArray photo = photos.getJSONArray("photo");
            photoInfoArrayList.clear();

            for (int i = 0; i < photo.length(); i++) {
                JSONObject photoInfo = photo.getJSONObject(i);

                String id = photoInfo.getString("id");
                String secret = photoInfo.getString("secret");
                String server = photoInfo.getString("server");
                String farm = photoInfo.getString("farm");
                String title = photoInfo.getString("title");

                String thumbnailPhotoURL = "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_t.jpg";
                String largePhotoURL = "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_b.jpg";

                showimageTask task = new showimageTask();
                task.execute(thumbnailPhotoURL, largePhotoURL);
            }
            return true;
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
        return false;
    }
}
