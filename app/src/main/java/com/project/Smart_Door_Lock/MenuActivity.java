package com.project.Smart_Door_Lock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.io.UnsupportedEncodingException;

public class MenuActivity extends AppCompatActivity  {
    static final String LOG_TAG = MenuActivity.class.getCanonicalName();

    LinearLayout llDoor;
    LinearLayout llText;
    LinearLayout llLive;
    LinearLayout llSiren;
    LinearLayout llPicture;
    LinearLayout llVoice;
    LinearLayout llHistory;
    LinearLayout llReboot;
    LinearLayout llSettings;


    ImageView imgPiStatus;

    private int offCount = 0;
    private boolean isPiOn = false;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        connectionCheck();
        startSubscribe();
        raspConnectionCheck();

        llDoor = (LinearLayout) findViewById(R.id.llDoor);
        llDoor.setOnClickListener(doorClick);

        llText = (LinearLayout) findViewById(R.id.llText);
        llText.setOnClickListener(TTSClick);

        llLive = (LinearLayout) findViewById(R.id.llLive);
        llLive.setOnClickListener(streamingClick);

        llSiren = (LinearLayout) findViewById(R.id.llSiren);
        llSiren.setOnClickListener(sirenClick);

        llPicture = (LinearLayout) findViewById(R.id.llPicture);
        llPicture.setOnClickListener(pictureClick);

        llVoice = (LinearLayout) findViewById(R.id.llVoice);
        llVoice.setOnClickListener(voiceMessageClick);

        llHistory = (LinearLayout) findViewById(R.id.llHistory);
        llHistory.setOnClickListener(historyClick);

        llReboot = (LinearLayout) findViewById(R.id.llReboot);
        llReboot.setOnClickListener(rebootClick);

        llSettings = (LinearLayout) findViewById(R.id.llSettings);
        llSettings.setOnClickListener(settingClick);
        imgPiStatus = findViewById(R.id.imgPiStatus);

        preferences =  getSharedPreferences("Shared preference", Context.MODE_PRIVATE);
    }





    View.OnClickListener doorClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPiOn) {
                try {
                    MainActivity.mqttManager.publishString("Door button", "Door", AWSIotMqttQos.QOS0);
                    Toast.makeText(MenuActivity.this, "Door is opened", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
            }
            else
                Toast.makeText(MenuActivity.this, "Raspberry pi is off", Toast.LENGTH_SHORT).show();

        }
    };

    View.OnClickListener TTSClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPiOn) {
                Intent intent = new Intent(getApplicationContext(), TTSActivity.class);
                startActivity(intent);
            }
            else
                Toast.makeText(MenuActivity.this, "Raspberry pi is off", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener sirenClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPiOn) {
                try {
                    MainActivity.mqttManager.publishString("Ring srien", "Siren", AWSIotMqttQos.QOS0);
                    Toast.makeText(MenuActivity.this, "Siren is ringing", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
            }
            else
                Toast.makeText(MenuActivity.this, "Raspberry pi is off", Toast.LENGTH_SHORT).show();

        }
    };

    View.OnClickListener rebootClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPiOn) {
                try {
                    MainActivity.mqttManager.publishString("reboot", "Reboot", AWSIotMqttQos.QOS0);
                    Toast.makeText(MenuActivity.this, "Raspberry PI is rebooted", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
            }
            else
                Toast.makeText(MenuActivity.this, "Raspberry pi is off", Toast.LENGTH_SHORT).show();

        }
    };

    View.OnClickListener streamingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isPiOn) {
                Intent intent = new Intent(getApplicationContext(), StreamingActivity.class);
                startActivity(intent);
            }
            else
                Toast.makeText(MenuActivity.this, "Raspberry pi is off", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener settingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener pictureClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), AWSFileLoaderActivity.class);
            intent.putExtra("Type", Constants.PICTURE_BUCKET_NAME);
            startActivity(intent);
        }
    };

    View.OnClickListener voiceMessageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), AWSFileLoaderActivity.class);
            intent.putExtra("Type", Constants.VOICE_MESSAGE_BUCKET_NAME);
            startActivity(intent);
        }
    };

    View.OnClickListener historyClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
            startActivity(intent);
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
                                Toast.makeText(MenuActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            } else {
                                startActivity(intent);
                                Toast.makeText(MenuActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
        }
    }

    public void startSubscribe()
    {
        try {
            MainActivity.mqttManager.subscribeToTopic("Raspberry pi status", AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        imgPiStatus.setImageResource(R.drawable.presence_online);
                                        offCount = 0;
                                        isPiOn = true;
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }


    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            imgPiStatus.setImageResource(R.drawable.presence_offline);
            isPiOn = false;
        }

    };

    public void raspConnectionCheck()
    {
        Thread thread = new Thread() {
            public void run() {
                while (true){
                    if(offCount >= 5)
                    {
                        Message msg = handler.obtainMessage();

                        handler.sendMessage(msg);
                    }

                    offCount ++;
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }
}
