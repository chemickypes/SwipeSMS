package com.baraccasoftware.swipesms.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.baraccasoftware.swipesms.app.component.ComposeSwipeTouchListener;
import com.baraccasoftware.swipesms.app.component.ConversationListAdapter;
import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.object.ConversationListLoader;

import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the
 * interface.
 */
public class ConversationFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Conversation>> {

    private static final String TAG = "ConversationFragment";
    public static final String CONVERSATIONLIST_CHANGED = "com.baraccasoftware.swipesms.CONVERSATIONLIST_CHANGED";
    private static final int LOADER_ID = 0;


    private OnItemClickFragmentListener mListener;
    private ComposeSwipeTouchListener mComposeListener;
    private ComposeSwipeTouchListener.ComposeCallbacks mComposeCallbacks;

    private ConversationListAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAdapter = new ConversationListAdapter(getActivity(),R.layout.conversation_row);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemClickFragmentListener) activity;
            mComposeCallbacks = (ComposeSwipeTouchListener.ComposeCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mComposeListener = new ComposeSwipeTouchListener(getListView(),mComposeCallbacks);
        getListView().setOnTouchListener(mComposeListener);
        getListView().setOnScrollListener(mComposeListener.makeScrollListener());

        if(mListener != null) mListener.registerContextMenu(getListView());
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onLongItemClick(getListView(),mAdapter.getItem(i));
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID,null,this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Conversation c = mAdapter.getItem(position);
        if(c == null){
            Log.d(TAG, "conversation null");
            //getLoaderManager().restartLoader(LOADER_ID,null,this);
        }

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onItemClick(c);
        }
    }

    @Override
    public Loader<List<Conversation>> onCreateLoader(int i, Bundle bundle) {
        return new ConversationListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Conversation>> listLoader, List<Conversation> conversations) {

        // carico lista sull'adapter
        mAdapter.setData(conversations);
        if(isResumed()){
            setListShown(true);
        }else{
            setListShownNoAnimation(true);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Conversation>> listLoader) {
        //  carico null sull'adapter
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
    public interface OnItemClickFragmentListener {
        public void onLongItemClick(ListView l,Conversation conversation);
        public void onItemClick(Conversation c);
        public void registerContextMenu(ListView l);
    }

}
