package io.google.anywhere_vocab;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClipboardMonitorStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            ComponentName service = context.startService(
                    new Intent(context, MyService.class));
        } else {
            Log.e("Hii", "Recieved unexpected intent " + intent.toString());
        }
    }
}
