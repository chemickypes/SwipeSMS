package com.baraccasoftware.swipesms.app.component;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baraccasoftware.swipesms.app.R;
import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.util.ImageLoader;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by angelo on 16/04/14.
 */
public class ConversationListAdapter extends ArrayAdapter<Conversation>{

    private Context mContext;
    private int mResourceLayout;
    private ImageLoader mImageLoader;

    public ConversationListAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mResourceLayout = resource;
        mImageLoader = new ImageLoader(mContext);
    }

    public void setData(List<Conversation> data){
        clear();
        if(data != null){
            for(Conversation c:data){
                add(c);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView person;
        TextView data;
        ImageView imageView;

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(mResourceLayout,null);


        }

        person = (TextView) convertView.findViewById(R.id.person_conversationlist_textview);
        data = (TextView) convertView.findViewById(R.id.date_conversationlist_textView);
        imageView = (ImageView) convertView.findViewById(R.id.img_conversationlis_imageView);

        Conversation c = getItem(position);
        if( c.getPerson() != null){
            person.setText(c.getPerson());
        }else{
            person.setText(c.getAddress());
        }

        if(SwipeSMSProvider.getIdUnReadSMS(mContext, PhoneNumberUtils.stripSeparators(c.getAddress())) != -1){
            Log.d("TAGGONE", c.getAddress() + " dentro");
            person.setTextColor(mContext.getResources().getColor(R.color.text_red));
        }else{
            person.setTextColor(mContext.getResources().getColor(android.R.color.black));
        }

        data.setText(getDateString(c.getDate()));

        //chiamo imageloader per caricare la foto
        mImageLoader.loadBitimap(c,imageView);
        return convertView;
    }


    private String getDateString(long date){
        String dateString;
        SimpleDateFormat f;
        long current = new Date().getTime();
        long diff = (current - date)/(24 * 60 * 60 * 1000);
        f = new SimpleDateFormat("dd");
        if(diff<1 && f.format(current).equals(f.format(date))){
            //today
            f = new SimpleDateFormat("HH:mm");
            dateString = f.format(date);
        }else{
            f = new SimpleDateFormat("dd/MMM/yy");

        }

        dateString = f.format(date);
        return dateString;
    }
}
