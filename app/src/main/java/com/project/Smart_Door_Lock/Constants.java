package com.project.Smart_Door_Lock;

import android.os.ParcelUuid;

/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {

    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     *
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     * {@link https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery}
     */
    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000b81d-0000-1000-8000-00805f9b34fb");

    public static final int REQUEST_ENABLE_BT = 1;

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "eu-west-2:0b3b1fe7-dbb8-4f64-a2f3-6ab140318cb2";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "eu-west-2";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String PICTURE_BUCKET_NAME = "visitorpicturebucket";
    public static final String VOICE_MESSAGE_BUCKET_NAME = "voicemsgbucket";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-east-1";

    public static final String STREAMING_URL = "https://player.twitch.tv/?channel=gkdlfndxogm";

}