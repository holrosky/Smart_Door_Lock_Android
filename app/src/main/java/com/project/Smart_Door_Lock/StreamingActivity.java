/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.project.Smart_Door_Lock;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class StreamingActivity extends AppCompatActivity {
    static final String LOG_TAG = StreamingActivity.class.getCanonicalName();

    EditText txtMessage;
    ImageButton btnPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);

        connectionCheck();

        txtMessage = (EditText) findViewById(R.id.txtMessage);

        btnPublish = (ImageButton) findViewById(R.id.btnPublish);
        btnPublish.setOnClickListener(publishClick);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(new WebViewFragment(), "SAMPLE_FRAG");
        ft.commit();
    }

    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = txtMessage.getText().toString();

            try {
                MainActivity.mqttManager.publishString(msg, "TTS", AWSIotMqttQos.QOS0);

                txtMessage.setText("");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };

    public void connectionCheck(){
        try {
            MainActivity.mqttManager.connect(MainActivity.clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status == AWSIotMqttClientStatus.Connecting) {

                            } else if (status == AWSIotMqttClientStatus.Connected) {

                            } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                            } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                                startActivity(intent);
                                Toast.makeText(StreamingActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            } else {
                                startActivity(intent);
                                Toast.makeText(StreamingActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
        }
    }
}
