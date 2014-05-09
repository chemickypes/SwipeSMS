package com.baraccasoftware.swipesms.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.baraccasoftware.swipesms.app.component.SMSNotification;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 *
 */
public class MarskAsReadIntentService extends IntentService {

    public static final String ACTION_MARK_AS_READ = "com.baraccasoftware.swipesms.app.service.action.MARK_AS_READ";



    public static final String EXTRA_ADDRESS = "com.baraccasoftware.swipesms.app.service.extra.ADDRESS";
    public static final String EXTRA_NOT_ID = "com.baraccasoftware.swipesms.app.service.extra.NOT_ID";


    public MarskAsReadIntentService() {
        super("MarskAsReadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MARK_AS_READ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_ADDRESS);
                final int param2 = intent.getIntExtra(EXTRA_NOT_ID,-1);
                handleActionMarkAsRead(param1,param2);
            }
        }

        stopSelf();

    }

    private void handleActionMarkAsRead(String address, int not_id) {
        Log.i("MarkAsReadIntentService", "address: "+address+" not_id: "+not_id);
        SwipeSMSProvider.removeUnReadSMS(this,address);
        SMSNotification.cancel(this, not_id);
    }



}
