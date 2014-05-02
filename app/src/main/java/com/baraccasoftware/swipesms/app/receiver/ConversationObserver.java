package com.baraccasoftware.swipesms.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baraccasoftware.swipesms.app.ConversationFragment;
import com.baraccasoftware.swipesms.app.object.ConversationListLoader;

/**
 * Created by angelo on 16/04/14.
 */
public class ConversationObserver extends BroadcastReceiver {
    private ConversationListLoader mLoader;

    public ConversationObserver(ConversationListLoader loader){
        mLoader = loader;

        mLoader.getContext().registerReceiver(this, new IntentFilter(ConversationFragment.CONVERSATIONLIST_CHANGED));
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mLoader.onContentChanged();
    }
}
