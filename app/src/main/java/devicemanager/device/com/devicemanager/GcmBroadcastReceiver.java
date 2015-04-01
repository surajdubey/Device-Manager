package devicemanager.device.com.devicemanager;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;
import static devicemanager.device.com.devicemanager.CommonUtilities.Logd;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG= "ODMGcmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        //Toast.makeText(context, "U reached", Toast.LENGTH_LONG).show();
        Logd(TAG, "In receiver");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), HelperIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}