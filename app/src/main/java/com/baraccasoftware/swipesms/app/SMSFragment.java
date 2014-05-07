package com.baraccasoftware.swipesms.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.app.ListFragment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.baraccasoftware.swipesms.app.component.SMSListAdapter;
import com.baraccasoftware.swipesms.app.object.SMS;
import com.baraccasoftware.swipesms.app.object.SMSListLoader;
import com.baraccasoftware.swipesms.app.object.SwipeSMSContact;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;
import com.baraccasoftware.swipesms.app.util.Utils;

import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the
 * interface.
 */
public class SMSFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<SMS>> {
    public static final String NEW_SMS_CONVERSATION = "com.baraccasofwtare.swipesms.NEW_SMS_CONVERSATION";
    private static final String TAG = "SMS Fragment";

    private OnLongItemClickFragmentListener mListener;
    private SMSListAdapter mAdapter;



    private String person;
    private String address;
    private int thread_id;

    private final static int LOADER_ID = 1;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SMSFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setAdapterInfo();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLongItemClickFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        ListView l = (ListView) view.findViewById(android.R.id.list);
        l.setStackFromBottom(true);
        l.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        l.setDivider(null);
        return view;
        //return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mListener != null) mListener.registerContextMenu(getListView());
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onLongItemClick(getListView(),i);
                return true;
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if(SwipeSMSProvider.getIdUnReadSMS(getActivity(),getAddress())!= -1) {
            SwipeSMSProvider.removeUnReadSMS(getActivity(),getAddress());
            getLoaderManager().restartLoader(LOADER_ID,null,this);
        }*/
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //distruggo il loader
        getLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener && ((SMS) mAdapter.getItem(position)).getType() == SMS.TYPE_FAILED) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onItemClick(l, position);
        }
    }

    @Override
    public Loader<List<SMS>> onCreateLoader(int i, Bundle bundle) {
        SMSListLoader sl;
        if(Utils.hasKitKat()){
            //sl = new SMSListLoader(getActivity(),-1,address);
            thread_id = Integer.parseInt(SwipeSMSProvider.getThreadIdFromAddress(getActivity(),address));
        }

        sl = new SMSListLoader(getActivity(),thread_id,address);
        return sl;
    }

    @Override
    public void onLoadFinished(Loader<List<SMS>> listLoader, List<SMS> smses) {

        mAdapter.setData(smses);
        if(isResumed()){
            setListShown(true);
        }else{
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<SMS>> listLoader) {
        mAdapter.setData(null);
    }


    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnLongItemClickFragmentListener {
        // TODO: Update argument type and name
        public void onLongItemClick(ListView l,int id);
        public void onItemClick(ListView l, int id);
        public void registerContextMenu(ListView l);
    }

    public void setAdapterInfo(){
        mAdapter = new SMSListAdapter(getActivity());
        //Log.d(TAG,"thread_id: "+thread_id);
        setListAdapter(mAdapter);
        //getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
