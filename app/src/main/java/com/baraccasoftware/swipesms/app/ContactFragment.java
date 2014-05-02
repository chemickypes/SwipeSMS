package com.baraccasoftware.swipesms.app;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.baraccasoftware.swipesms.app.component.ContactAdapter;
import com.baraccasoftware.swipesms.app.object.SwipeSMSContact;
import com.baraccasoftware.swipesms.app.object.SwipeSMSContactLoader;
import com.baraccasoftware.swipesms.app.util.Utils;

import java.util.List;

/**
 * This fragment is called when we call new conversation to choice a contact to send sms
 */
public class ContactFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<SwipeSMSContact>>,
                                                        SearchView.OnQueryTextListener{

    private OnContactChoiceListener mListener;
    private ContactAdapter mAdapter;

    private SearchView mSearchView;
    private String patternToSearch = "";
    private View footer;
    private TextView footerTextView;

    public static final String CONTACTLIST_CHANGED = "com.baraccasoftware.swipesms.CONTACTLIST_CHANGED";
    private static final int LOADER_ID = 2;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set searchview
        //getListView().addHeaderView(mSearchView);
        mAdapter = new ContactAdapter(getActivity());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        setHasOptionsMenu(true);


    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnContactChoiceListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        ListView l = (ListView) view.findViewById(android.R.id.list);
        footer = inflater.inflate(R.layout.footer_layout_search_contact,null);
        footerTextView = (TextView) footer.findViewById(R.id.address_footer_textview);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG","footer");
                SwipeSMSContact c = new SwipeSMSContact();
                c.setName(patternToSearch);
                c.setAddress(patternToSearch);
                mListener.onContactChoice(c);
            }
        });
        l.setDivider(null);
        l.addFooterView(footer);
        footer.setVisibility(View.GONE);



        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        mSearchView = new SearchView(getActivity());
        mSearchView.setQueryHint(getString(R.string.to_contact));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(this);


        menu.add("Search")
                .setIcon(android.R.drawable.ic_menu_search)
                .setActionView(mSearchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onContactChoice(((SwipeSMSContact)mAdapter.getItem(position)));
        }
    }

    @Override
    public Loader<List<SwipeSMSContact>> onCreateLoader(int i, Bundle bundle) {

        return new SwipeSMSContactLoader(getActivity(),patternToSearch);
    }

    @Override
    public void onLoadFinished(Loader<List<SwipeSMSContact>> listLoader, List<SwipeSMSContact> swipeSMSContacts) {
        mAdapter.setData(swipeSMSContacts);
        if(isResumed()){
            setListShown(true);
        }else{
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<SwipeSMSContact>> listLoader) {
        mAdapter.setData(null);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        patternToSearch = s;
        if(Utils.isNumeric(patternToSearch)){
            footer.setVisibility(View.VISIBLE);
            footerTextView.setText(patternToSearch);
        }else{
            footer.setVisibility(View.GONE);

        }
        getLoaderManager().restartLoader(LOADER_ID,null, this);
        return false;
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
    public interface OnContactChoiceListener {

        public void onContactChoice(SwipeSMSContact swipeSMSContact);
    }

}
