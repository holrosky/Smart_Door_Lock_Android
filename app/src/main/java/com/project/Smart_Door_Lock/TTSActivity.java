package com.project.Smart_Door_Lock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

public class TTSActivity extends AppCompatActivity {
    static final String LOG_TAG = TTSActivity.class.getCanonicalName();

    TextView preMessage;
    TextView tvAuto1;
    TextView tvAuto2;
    TextView tvAuto3;
    TextView tvAuto4;
    EditText txtMessage;
    ImageButton btnPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        connectionCheck();

        preMessage = (TextView) findViewById(R.id.preMessage);
        preMessage.setEnabled(false);

        tvAuto1 = (TextView) findViewById(R.id.tvAuto1);
        tvAuto1.setOnClickListener(auto1Click);

        tvAuto2 = (TextView) findViewById(R.id.tvAuto2);
        tvAuto2.setOnClickListener(auto2Click);

        tvAuto3 = (TextView) findViewById(R.id.tvAuto3);
        tvAuto3.setOnClickListener(auto3Click);

        tvAuto4 = (TextView) findViewById(R.id.tvAuto4);
        tvAuto4.setOnClickListener(auto4Click);

        txtMessage = (EditText) findViewById(R.id.txtMessage);

        btnPublish = (ImageButton) findViewById(R.id.btnPublish);
        btnPublish.setOnClickListener(publishClick);

    }

    View.OnClickListener auto1Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = tvAuto1.getText().toString();

            publish(msg);
        }
    };


    View.OnClickListener auto2Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = tvAuto2.getText().toString();

            publish(msg);
        }
    };


    View.OnClickListener auto3Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = tvAuto3.getText().toString();

            publish(msg);
        }
    };


    View.OnClickListener auto4Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = tvAuto4.getText().toString();

            publish(msg);
        }
    };




    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = txtMessage.getText().toString();

            publish(msg);

        }
    };

    public void publish(String msg)
    {
        try {
            MainActivity.mqttManager.publishString(msg, "TTS", AWSIotMqttQos.QOS0);
            preMessage.append(msg+"\n");

            txtMessage.setText("");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }

    }

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
                                Toast.makeText(TTSActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            } else {
                                startActivity(intent);
                                Toast.makeText(TTSActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

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
