package com.project.Smart_Door_Lock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VisitorPictureViewActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        imageView = (ImageView) findViewById(R.id.imgPicture);

        Thread urlThread = new Thread() {
            @Override
            public void run() {
                try {
                    String url = getIntent().getStringExtra("url");

                    URL imgURL = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) imgURL.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream inputStream = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        urlThread.start();

        try
        {
            urlThread.join();

            imageView.setImageBitmap(bitmap);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
