package com.baraccasoftware.swipesms.app.object;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baraccasoftware.swipesms.app.ConversationFragment;
import com.baraccasoftware.swipesms.app.receiver.ConversationObserver;
import com.baraccasoftware.swipesms.app.receiver.SMSObserver;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;
import com.baraccasoftware.swipesms.app.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelo on 16/04/14.
 */
public class SMSListLoader extends AsyncTaskLoader<List<SMS>> {
    private final static String TAG = "SMS Loader";
    private Context mContext;
    private int thread_id;
    private String address;

    private List<SMS> mSMS;
    private SMSObserver mSmsObserver;

    private boolean DEBUG = false;

    public SMSListLoader(Context context,int thread_id, String address) {
        super(context);
        mContext = context;
        this.thread_id = thread_id;
        this.address = address;
    }

    @Override
    public List<SMS> loadInBackground() {

        List<SMS> list = new ArrayList<SMS>();
        if(thread_id != -1){
            //ricerca attraverso id
            if(SwipeSMSProvider.setReadSMS(mContext,thread_id))
                Utils.sendBCastToNotifyConvChangement(mContext);


            list = SwipeSMSProvider.getSMSList(mContext,thread_id);
        }else {
            //ricerca attraverso address
            list = SwipeSMSProvider.getSMSList(mContext,address);
        }
        if(DEBUG) Log.d(TAG,"List got, size: "+list.size());
        return list;
    }

    @Override
    public void deliverResult(List<SMS> data) {
        if(isReset()){
            if(data != null){
                releaseResource(data);
                return;
            }
        }

        List<SMS> oldSMS = mSMS;
        mSMS = data;

        if(isStarted()){
            super.deliverResult(data);
        }

        //invalidate old conversation
        if (oldSMS != null && oldSMS!= data) {
            releaseResource(oldSMS);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mSMS != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mSMS);
        }

        // Register the observers that will notify the Loader when changes are made.
        if (mSmsObserver == null) {
            mSmsObserver = new SMSObserver(this);
        }


        if (takeContentChanged()) {

            forceLoad();
        } else if (mSMS == null) {
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
        if (mSMS != null) {
            releaseResource(mSMS);
            mSMS = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        if (mSmsObserver != null) {
            getContext().unregisterReceiver(mSmsObserver);
            mSmsObserver = null;
        }


    }

    @Override
    public void onCanceled(List<SMS> data) {
        super.onCanceled(data);
        releaseResource(data);
    }

    private void releaseResource(List<SMS> data){}


}
