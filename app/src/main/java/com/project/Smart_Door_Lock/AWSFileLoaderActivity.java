/*
 * Copyright 2015-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.project.Smart_Door_Lock;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DownloadSelectionActivity displays a list of files in the bucket. Users can
 * select a file to download.
 */
public class AWSFileLoaderActivity extends ListActivity {
    static final String LOG_TAG = AWSFileLoaderActivity.class.getCanonicalName();

    // The S3 client used for getting the list of objects in the bucket
    private AmazonS3Client s3;
    // An adapter to show the objects
    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, Object>> transferRecordMaps;
    private Util util;
    private java.util.Date expiration = new java.util.Date();
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_loader);
        util = new Util();
        initData();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the file list.
        new GetFileListTask().execute();
    }

    private void initData() {
        // Gets the default S3 client.
        s3 = util.getS3Client(AWSFileLoaderActivity.this);
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
    }

    private void initUI() {
        simpleAdapter = new SimpleAdapter(this, transferRecordMaps,
                R.layout.bucket_item, new String[] {
                        "key"
                },
                new int[] {
                        R.id.key
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.key:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        fileName.setTextColor(Color.BLACK);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        // When an item is selected, finish the activity and pass back the S3
        // key associated with the object selected
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                long expTimeMillis = expiration.getTime();
                expTimeMillis += 1000 * 60 * 60;
                expiration.setTime(expTimeMillis);

                if(getIntent().getStringExtra("Type").equals(Constants.PICTURE_BUCKET_NAME)) {
                    GeneratePresignedUrlRequest generatePresignedUrlRequest =
                            new GeneratePresignedUrlRequest(Constants.PICTURE_BUCKET_NAME, (String) transferRecordMaps.get(pos).get("key"))
                                    .withMethod(HttpMethod.GET)
                                    .withExpiration(expiration);

                    URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);

                    Log.i(LOG_TAG, "Pre-signed URL : " + url.toString());

                    Intent intent = new Intent(getApplicationContext(), VisitorPictureViewActivity.class);
                    intent.putExtra("url", url.toString());

                    startActivity(intent);
                }
                else if(getIntent().getStringExtra("Type").equals(Constants.VOICE_MESSAGE_BUCKET_NAME)){
                    GeneratePresignedUrlRequest generatePresignedUrlRequest =
                            new GeneratePresignedUrlRequest(Constants.VOICE_MESSAGE_BUCKET_NAME, (String) transferRecordMaps.get(pos).get("key"))
                                    .withMethod(HttpMethod.GET)
                                    .withExpiration(expiration);

                    URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);

                    Log.i(LOG_TAG, "Pre-signed URL : " + url.toString());


                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    try {
                        mediaPlayer.setDataSource(url.toString());
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Error occurred! Try again", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    mediaPlayer.start();
                }
            }
        });
    }

    /**
     * This async task queries S3 for all files in the given bucket so that they
     * can be displayed on the screen
     */
    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(AWSFileLoaderActivity.this,
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Queries files in the bucket from S3.
            String bucket_name = getIntent().getStringExtra("Type");
            s3ObjList = s3.listObjects(bucket_name).getObjectSummaries();
            transferRecordMaps.clear();
            for (S3ObjectSummary summary : s3ObjList) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", summary.getKey());
                transferRecordMaps.add(map);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            simpleAdapter.notifyDataSetChanged();
        }
    }
}
