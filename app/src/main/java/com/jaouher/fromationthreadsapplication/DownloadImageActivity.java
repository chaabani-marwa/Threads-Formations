package com.jaouher.fromationthreadsapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadImageActivity extends AppCompatActivity {

    private static final String TAG = "DownloadImageActivity";
    private ProgressBar progressBar1, progressBar2;
    private TextView textViewIndicator1, textViewIndicator2, counter;
    private RecyclerView recyclerView;
    private AppCompatImageButton downloadBtn, plusBtn;
    private RecyclerAdapter recyclerAdapter;

    private URL url1, url2, url3, url4;

    private DownloadImageAsyncTask downloadImageAsyncTask;

    int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_image);


        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);
        recyclerView = findViewById(R.id.recyclerView);
        downloadBtn = findViewById(R.id.downloadBtn);
        plusBtn = findViewById(R.id.plusBtn);
        textViewIndicator1 = findViewById(R.id.textViewProgress1);
        textViewIndicator2 = findViewById(R.id.textViewProgress2);
        counter = findViewById(R.id.counter);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        progressBar1.setProgress(0);
        progressBar2.setProgress(0);

        plusBtn.setOnClickListener(v ->{
            i++;
            counter.setText(String.valueOf(i));
        });

        try {
            url1 = new URL("https://i.pinimg.com/originals/2b/2b/11/2b2b1110569856cc7b4962abf6695e9c.jpg");
            url2 = new URL("https://images.pexels.com/photos/853199/pexels-photo-853199.jpeg?auto=compress&cs=tinysrgb&dpr=3&h=750&w=1260");
            url3 = new URL("https://images.unsplash.com/photo-1553152531-b98a2fc8d3bf?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1789&q=80");
            url4 = new URL("https://images.unsplash.com/photo-1552839335-ab4d3df45a0e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1552&q=80");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        downloadBtn.setOnClickListener(v -> {
            Log.i(TAG, "url : " + url1);
            new DownloadImageAsyncTask().execute(url1, url2, url3, url4);
        });

    }

    class DownloadImageAsyncTask extends AsyncTask<URL, Integer, Bitmap[]> {
        float nbrOfFileDownload ;

        private static final String TAG = "DownloadImageAsyncTask" ;
        private int urlCount = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar1.setVisibility(View.VISIBLE);
            progressBar2.setVisibility(View.VISIBLE);
            textViewIndicator1.setVisibility(View.VISIBLE);
            textViewIndicator2.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap[] doInBackground(URL... urls) {
            urlCount = urls.length;
            Bitmap[] bitmaps = new Bitmap[urls.length];

            for (int i = 0; i < urlCount; i++) {
                nbrOfFileDownload = i +1;
                bitmaps[i] = downloadImage(urls[i]);
            }

            return bitmaps;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progressBar1.setProgress(values[0]);
            textViewIndicator1.setText(values[0] + "%");

            int fileProgress =  ((int)nbrOfFileDownload /urlCount) *100;
            Log.i(TAG,"fileProgress "+fileProgress );

            progressBar2.setProgress(fileProgress);
            textViewIndicator2.setText( (int)nbrOfFileDownload + "/" + urlCount);

        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);

            recyclerAdapter.bitmaps = bitmaps;
            recyclerAdapter.notifyDataSetChanged();
        }


        private Bitmap downloadImage(URL url) {
            Bitmap image;
            if (checkPermission()) {
                try {
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    int fileLength = connection.getContentLength();


                    InputStream inputStream = new BufferedInputStream(url.openStream());
                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;


                    while ((count = inputStream.read(data)) != -1) {
                        total += count;
                        publishProgress((int) (total * 100) / fileLength);
                    }

                    Long ts = System.currentTimeMillis();
                    String imageName = ts.toString() + ".jpeg";
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    inputStream.close();

                    saveImageToExternal(imageName, image);

                    return image;


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void saveImageToExternal(String imageName, Bitmap image) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "_Formation");
            path.mkdir();
            File imageFile = new File(path, imageName);
            try {
                FileOutputStream out = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET }, 0);
            return false;
        } else {
            return true;
        }
    }
}
