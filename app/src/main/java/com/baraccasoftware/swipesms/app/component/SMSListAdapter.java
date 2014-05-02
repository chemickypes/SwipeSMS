package com.baraccasoftware.swipesms.app.component;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baraccasoftware.swipesms.app.R;
import com.baraccasoftware.swipesms.app.object.SMS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelo on 16/04/14.
 */
public class SMSListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<SMS> mData;

    private boolean DEBUG = false;
    private String TAG = "SMS Adapter";


    public SMSListAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<SMS>();
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView bodyMessage;
        ImageView imageview;
        SMS sms = (SMS) getItem(i);
        if(DEBUG) Log.d(TAG, "sms type: "+ sms.getType());
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(sms.getType() == 1){
                view = layoutInflater.inflate(R.layout.message_row,null);
            }else {
                view = layoutInflater.inflate(R.layout.message_row_sent,null);
            }

        imageview = (ImageView) view.findViewById(R.id.message_sent_imageView);
        if(imageview != null) {
            if (sms.getType() == SMS.TYPE_FAILED) {
                imageview.setVisibility(View.VISIBLE);
            } else {
                imageview.setVisibility(View.GONE);
            }
        }

        bodyMessage = (TextView) view.findViewById(R.id.message_textView);
        if(sms.getType() == SMS.TYPE_OUTBOX){
            bodyMessage.setAlpha(0.5f);
        }else {
            bodyMessage.setAlpha(1f);
        }



        bodyMessage.setText(sms.getBody());

        return view;
    }

    public void setData(List<SMS> list){
        mData.clear();
        if(list != null){
            for(SMS s:list){
                mData.add(s);
            }
        }
        notifyDataSetChanged();
    }
}
