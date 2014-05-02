package com.baraccasoftware.swipesms.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baraccasoftware.swipesms.app.SMSFragment;
import com.baraccasoftware.swipesms.app.object.SMSListLoader;

/**
 * Created by angelo on 16/04/14.
 */
public class SMSObserver extends BroadcastReceiver {
    private SMSListLoader loader;

    public SMSObserver(SMSListLoader loader) {
        this.loader = loader;

        this.loader.getContext().registerReceiver(this,new IntentFilter(SMSFragment.NEW_SMS_CONVERSATION));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        loader.onContentChanged();

    }
}
