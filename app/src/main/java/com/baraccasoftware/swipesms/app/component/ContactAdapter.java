package com.baraccasoftware.swipesms.app.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.baraccasoftware.swipesms.app.R;
import com.baraccasoftware.swipesms.app.object.SwipeSMSContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelo on 20/04/14.
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer{
    ;
    private Context context;
    private AlphabetIndexer mAlphabetIndexer;

    private ArrayList<SwipeSMSContact> mData;

    public ContactAdapter(Context context) {

        this.context = context;
        mData = new ArrayList<SwipeSMSContact>();

        final String alphabet = context.getString(R.string.alphabet);

        // Instantiates a new AlphabetIndexer bound to the column used to sort contact names.
        // The cursor is left null, because it has not yet been retrieved.
        //mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contact_row, null);
            holder.name = (TextView) convertView.findViewById(R.id.person_contactList_textview);
            holder.address = (TextView) convertView.findViewById(R.id.address_contactlist_textView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        SwipeSMSContact sc = (SwipeSMSContact) getItem(position);
        try {

            holder.name.setText(sc.getName());
            holder.address.setText(sc.getAddress());
        }catch (NullPointerException ex){}

        return convertView;
    }

    public void setData(List<SwipeSMSContact> list){
        mData.clear();
        if(list != null){
            for(SwipeSMSContact sc : list){
                mData.add(sc);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int i) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
    }

    class ViewHolder{
        TextView name;
        TextView address;
    }
}
