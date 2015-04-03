package devicemanager.device.com.devicemanager;


import static devicemanager.device.com.devicemanager.CommonUtilities.Logd;
import static devicemanager.device.com.devicemanager.CommonUtilities.getVAR;
import static devicemanager.device.com.devicemanager.CommonUtilities.setVAR;
import static devicemanager.device.com.devicemanager.CommonUtilities.loadVARs;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class HelperIntentService extends IntentService {
    private static final String TAG= "ODMHelperIntentService";
    Context context;

    public HelperIntentService() {
        super("HelperIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        context = getApplicationContext();
        Bundle extras = intent.getExtras();
        String msg = intent.getStringExtra("message");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        Toast.makeText(context, "U reached", Toast.LENGTH_LONG).show();
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Logd(TAG, "Error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Logd(TAG, "Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // If it's a regular GCM message, do some work.
                try {
					loadVARs(getApplicationContext());
                    MCrypt mcrypt = new MCrypt();
                    String decrypted = new String(mcrypt.decrypt(msg));
                    Logd(TAG, "Received message: " + decrypted);
                    handleMessage(decrypted);
                } catch (Exception e) {
                    Logd(TAG, "Error: " + e.getMessage());
                }
            }
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleMessage(String message) {
        // Waking up mobile if it is sleeping
        //WakeLocker.acquire(getApplicationContext());
        if (message.startsWith("Command:Notify:")) {
            String notification = message.replaceFirst("Command:Notify:", "");
            generateNotification(context, notification);
        } else if (message.startsWith("Command:SMS:")) {
            String destinationAddress = message.replaceFirst("Command:SMS:", "");
            SmsManager sms = SmsManager.getDefault();
            try {
                sms.sendTextMessage(destinationAddress, null, "This number currently has this device.", null, null);
                Logd(TAG, "Sent SMS");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else if (message.equals("Command:Wipe")) {
            DevicePolicyManager mDPM;
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
        } else if (message.equals("Command:Lock")) {
            DevicePolicyManager mDPM;
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName mDeviceAdmin;
            mDeviceAdmin = new ComponentName(context, GetAdminReceiver.class);
            if (mDPM.isAdminActive(mDeviceAdmin)) {
                Logd(TAG, "Locking device");
                mDPM.lockNow();
            }
        } else if (message.startsWith("Command:LockPass:")) {
            String password = message.replaceFirst("Command:LockPass:", "");
            DevicePolicyManager mDPM;
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName mDeviceAdmin;
            mDeviceAdmin = new ComponentName(context, GetAdminReceiver.class);
            if (mDPM.isAdminActive(mDeviceAdmin)) {
                Logd(TAG, "Locking device with password");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                mDPM.setPasswordMinimumLength(mDeviceAdmin, 4);
                mDPM.resetPassword(password, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                mDPM.lockNow();
            }
        }
        // Releasing wake lock
        //WakeLocker.release();
    }

    // Issues a notification to inform the user that server has sent a message.
    @SuppressWarnings("deprecation")
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);
    }
}