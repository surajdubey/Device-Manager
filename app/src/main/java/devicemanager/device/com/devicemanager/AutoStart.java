package devicemanager.device.com.devicemanager;

/**
 * Created by suraj.
 */


import static devicemanager.device.com.devicemanager.CommonUtilities.Logd;
import static devicemanager.device.com.devicemanager.CommonUtilities.getVAR;
import static devicemanager.device.com.devicemanager.CommonUtilities.setVAR;
import static devicemanager.device.com.devicemanager.CommonUtilities.loadVARs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {
    private static final String TAG= "ODMAutoStart";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            loadVARs(context);
            String interval = getVAR("INTERVAL");
            String version = getVAR("VERSION");
        }
    }
}