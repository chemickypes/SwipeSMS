package com.baraccasoftware.swipesms.app.object;

import android.content.Context;
import android.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;

import java.util.List;

/**
 * Created by angelo on 20/04/14.
 */
public class SwipeSMSContactLoader extends AsyncTaskLoader<List<SwipeSMSContact>> {

    private Context mContext;
    private String pattern;

    private List<SwipeSMSContact> mContact;
    //private SwipeSMSContactObserver mContactObserver;

    public SwipeSMSContactLoader(Context mContext,String pattern) {
        super(mContext);
        this.mContext = mContext;
        this.pattern = pattern;
    }

    @Override
    public List<SwipeSMSContact> loadInBackground() {
        List<SwipeSMSContact> list;
        if(TextUtils.isEmpty(pattern)){
            list = SwipeSMSProvider.getAllContact(mContext);
        }else {
            list = SwipeSMSProvider.getAllContact(mContext,pattern);
        }
        return list;
    }

    @Override
    public void deliverResult(List<SwipeSMSContact> data) {
        if(isReset()){
            if(data != null){
                releaseResource(data);
                return;
            }
        }

        List<SwipeSMSContact> oldContact = mContact;
        mContact = data;

        if(isStarted()){
            super.deliverResult(data);
        }

        //invalidate old conversation
        if (oldContact != null && oldContact != data) {
            releaseResource(oldContact);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mContact != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mContact);
        }



        // Register the observers that will notify the Loader when changes are made.
        /*if (mContactObserver == null) {
            mContactObserver = new SwipeSMSContactObserver(this); //TODO
        }*/


        if (takeContentChanged()) {

            forceLoad();
        } else if (mContact == null) {
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
        if (mContact != null) {
            releaseResource(mContact);
            mContact = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        /*if (mContactObserver != null) {
            getContext().unregisterReceiver(mContactObserver);
            mContactObserver = null;
        }*/


    }

    @Override
    public void onCanceled(List<SwipeSMSContact> data) {
        super.onCanceled(data);
        releaseResource(data);
    }

    private void releaseResource(List<SwipeSMSContact> data){}
}
