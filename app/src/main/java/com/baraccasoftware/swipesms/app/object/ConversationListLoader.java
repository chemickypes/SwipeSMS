package com.baraccasoftware.swipesms.app.object;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;


import com.baraccasoftware.swipesms.app.receiver.ConversationObserver;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;

import java.util.List;

/**
 * Created by angelo on 16/04/14.
 */
public class ConversationListLoader extends AsyncTaskLoader<List<Conversation>> {

    private Context mContext;

    private List<Conversation> mConversation;
    private ConversationObserver mConversationObserver;

    public ConversationListLoader(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public List<Conversation> loadInBackground() {
        List<Conversation> list = SwipeSMSProvider.getConversationList(mContext);
        return list;
    }

    @Override
    public void deliverResult(List<Conversation> data) {
        if(isReset()){
            if(data != null){
                releaseResource(data);
                return;
            }
        }

        List<Conversation> oldConversations = mConversation;
        mConversation = data;

        if(isStarted()){
            super.deliverResult(data);
        }

        //invalidate old conversation
        if (oldConversations != null && oldConversations != data) {
            releaseResource(oldConversations);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mConversation != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mConversation);
        }

        // Register the observers that will notify the Loader when changes are made.
        if (mConversationObserver == null) {
            mConversationObserver = new ConversationObserver(this);
        }


        if (takeContentChanged()) {

            forceLoad();
        } else if (mConversation == null) {
            // If the current data is null... then we should make it non-null! :)
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'apps'.
        if (mConversation != null) {
            releaseResource(mConversation);
            mConversation = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        if (mConversationObserver != null) {
            getContext().unregisterReceiver(mConversationObserver);
            mConversationObserver = null;
        }


    }

    @Override
    public void onCanceled(List<Conversation> data) {
        super.onCanceled(data);
        releaseResource(data);
    }

    private void releaseResource(List<Conversation> data){}
}
