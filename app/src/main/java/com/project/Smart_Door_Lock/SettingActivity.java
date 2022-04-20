package com.project.Smart_Door_Lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class SettingActivity extends AppCompatActivity {
    static final String LOG_TAG = SettingActivity.class.getCanonicalName();

    Switch swPush;
    Switch swBLE;
    Switch swShake;
    LinearLayout layoutToken;
    LinearLayout layoutDisconnect;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private BluetoothAdapter mBluetoothAdapter;

    private ShakeDetector shakeDetector;

    ShakeOptions options = new ShakeOptions()
            .background(false)
            .interval(1000)
            .shakeCount(1)
            .sensibility(4.0f);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        connectionCheck();

        preferences =  getSharedPreferences("Shared preference", Context.MODE_PRIVATE);
        editor = preferences.edit();

        swPush = (Switch) findViewById(R.id.swPush);
        swPush.setOnClickListener(swPuchClick);
        swPush.setChecked(preferences.getBoolean("push flag", true));
        swPush.setText(preferences.getString("push string", "Push alarm ON"));

        swBLE = (Switch) findViewById(R.id.swBLE);
        swBLE.setOnClickListener(swBLEClick);
        swBLE.setChecked(preferences.getBoolean("BLE flag", false));
        swBLE.setText(preferences.getString("BLE string", "Bluetooth LE OFF"));

        swShake = (Switch) findViewById(R.id.swShake);
        swShake.setOnClickListener(swShakeClick);
        swShake.setChecked(preferences.getBoolean("shake flag", false));
        swShake.setText(preferences.getString("shake string", "Shake to Open OFF"));

        layoutToken = (LinearLayout) findViewById(R.id.layoutToken);
        layoutToken.setOnClickListener(tokenClick);

        layoutDisconnect = (LinearLayout) findViewById(R.id.layoutDisconnect);
        layoutDisconnect.setOnClickListener(disconnectClick);

        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();

        shakeDetector = new ShakeDetector(options);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


    View.OnClickListener swPuchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(swPush.isChecked())
            {
                Toast.makeText(SettingActivity.this, "Push alarm ON", Toast.LENGTH_SHORT).show();
                editor.putBoolean("push flag", true);
                editor.putString("push string", "Push alarm ON");
            }
            else
            {
                Toast.makeText(SettingActivity.this, "Push alarm OFF", Toast.LENGTH_SHORT).show();
                editor.putBoolean("push flag", false);
                editor.putString("push string", "Push alarm OFF");
            }

            editor.apply();
            editor.commit();
            swPush.setText(preferences.getString("push string", "Push alarm ON"));

        }
    };

    View.OnClickListener swBLEClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        if(preferences.getBoolean("BLE flag", false))
                        {
                            editor.putBoolean("BLE flag", false);
                            editor.putString("BLE string", "Bluetooth LE OFF");
                            editor.putBoolean("shake flag", false);
                            editor.putString("shake string", "Shake to Open OFF");
                            stopAdvertising();
                            shakeDetector.stopShakeDetector(getBaseContext());
                            Toast.makeText(SettingActivity.this, "Bluetooth Advertisements is OFF", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            editor.putBoolean("BLE flag", true);
                            editor.putString("BLE string", "Bluetooth LE ON");
                            startAdvertising();
                            Toast.makeText(SettingActivity.this, "Bluetooth Advertisements is ON", Toast.LENGTH_SHORT).show();
                        }

                        editor.apply();
                        editor.commit();
                        swBLE.setText(preferences.getString("BLE string", "Bluetooth Advertisements is ON"));
                        swShake.setChecked(preferences.getBoolean("shake flage", false));
                        swShake.setText(preferences.getString("shake string", "Shake to Open OFF"));


                    } else {

                        // Bluetooth Advertisements are not supported.
                        swBLE.setChecked(preferences.getBoolean("BLE flag", false));
                        Log.e(LOG_TAG, "Bluetooth Advertisements are not supported");
                        Toast.makeText(SettingActivity.this, "Bluetooth Advertisements are not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    swBLE.setChecked(preferences.getBoolean("BLE flag", false));
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                }
            } else {

                // Bluetooth is not supported.
                swBLE.setChecked(preferences.getBoolean("BLE flag", false));
                Log.e(LOG_TAG, "Bluetooth is not supported.");
                Toast.makeText(SettingActivity.this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            }
        }
    };

    View.OnClickListener swShakeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(swBLE.isChecked())
            {
                if(preferences.getBoolean("shake flag", false))
                {
                    shakeDetector.stopShakeDetector(getBaseContext());
                    Toast.makeText(SettingActivity.this, "Shake to open is OFF", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("shake flag", false);
                    editor.putString("shake string", "Shake to Open OFF");
                }
                else
                {
                    shakeDetector.start(getApplicationContext(), new ShakeCallback() {
                        @Override
                        public void onShake() {
                            try {
                                MainActivity.mqttManager.publishString("Shake", "Door", AWSIotMqttQos.QOS0);
                                Log.d(LOG_TAG, "Phone shaked to open the door");
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Publish error.", e);
                            }
                        }
                    });
                    Toast.makeText(SettingActivity.this, "Shake to open is ON", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("shake flag", true);
                    editor.putString("shake string", "Shake to Open ON");
                }

                editor.apply();
                editor.commit();
                swShake.setText(preferences.getString("shake string", "Shake to open is OFF"));
            }
            else
            {
                swShake.setChecked(preferences.getBoolean("shake flag", false));
                Log.e(LOG_TAG, "BLE is not on");
                Toast.makeText(SettingActivity.this, "Turn on BLE first", Toast.LENGTH_SHORT).show();
            }
        }
    };



    View.OnClickListener tokenClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(getApplicationContext(), Token.class);
            startActivity(intent);

        }
    };


    View.OnClickListener disconnectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                MainActivity.mqttManager.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
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
                                Toast.makeText(SettingActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            } else {
                                startActivity(intent);
                                Toast.makeText(SettingActivity.this, "MQTT server not connected.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
        }
    }

    /**
     * Starts BLE Advertising by starting.
     */
    private void startAdvertising() {
        Intent intent = new Intent(getApplicationContext(), AdvertiserService.class);
        startService(intent);

    }

    /**
     * Stops BLE Advertising by starting.
     */
    private void stopAdvertising() {
        Intent intent = new Intent(getApplicationContext(), AdvertiserService.class);
        stopService(intent);

    }
}
