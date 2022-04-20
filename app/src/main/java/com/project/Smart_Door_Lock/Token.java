package com.project.Smart_Door_Lock;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.google.firebase.iid.FirebaseInstanceId;

public class Token extends AppCompatActivity {
    static final String LOG_TAG = Token.class.getCanonicalName();

    TextView tvToken;
    ImageButton btnTokenCopy;
    String token = FirebaseInstanceId.getInstance().getToken();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        connectionCheck();

        tvToken = (TextView) findViewById(R.id.tvToken);

        btnTokenCopy = (ImageButton) findViewById(R.id.btnTokenCopy);
        btnTokenCopy.setOnClickListener(tokenCopyClick);

        tvToken.setText(token);

    }


    View.OnClickListener tokenCopyClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get token
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Token",token);
            clipboardManager.setPrimaryClip(clipData);

            // Log and toast
            String msg = getString(R.string.msg_token_fmt, token);
            Log.d(LOG_TAG, msg);
            Toast.makeText(Token.this, "Copied to clip board.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(Token.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            } else {
                                startActivity(intent);
                                Toast.makeText(Token.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

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
