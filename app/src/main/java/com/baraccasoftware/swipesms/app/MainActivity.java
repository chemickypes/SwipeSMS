package com.baraccasoftware.swipesms.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baraccasoftware.swipesms.app.component.ComposeSwipeTouchListener;
import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;
import com.baraccasoftware.swipesms.app.util.Utils;


public class MainActivity extends Activity implements ConversationFragment.OnItemClickFragmentListener,
        ComposeSwipeTouchListener.ComposeCallbacks{

    private static final String TAG = "MAIN ACTIVITY";
    private ConversationFragment mFragment;

    private Conversation mConversationSelectedFromContextMenu;
    private RelativeLayout mSetDefaultSmsLayout;

    //private SharedPreferences mSharedPreferences;

    //this bCast is to say if this activity is in foreground or not
    private BroadcastReceiver mNewMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragment = new ConversationFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();

        mSetDefaultSmsLayout = (RelativeLayout) findViewById(R.id.set_default_sms_layout);
        //mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mNewMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isOrderedBroadcast()) setResultCode(RESULT_OK);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNewMessageReceiver != null) registerReceiver(mNewMessageReceiver,
                new IntentFilter(ConversationActivity.NEW_MESSAGE));

        if (Utils.isDefaultSmsApp(this)) {
            // This app is the default, remove the "make this app the default" layout and
            // enable message sending components.
            mSetDefaultSmsLayout.setVisibility(View.GONE);

        } else {
            // Not the default, show the "make this app the default" layout and disable
            // message sending components.
            mSetDefaultSmsLayout.setVisibility(View.VISIBLE);


            Button button = (Button) findViewById(R.id.set_default_sms_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSetDefaultSmsLayout.setVisibility(View.GONE);
                    Utils.setDefaultSmsApp(MainActivity.this);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNewMessageReceiver != null) unregisterReceiver(mNewMessageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_context_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                //open settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_add:
                //start new conversation
                Intent i = new Intent(this,ConversationActivity.class);
                i.putExtra(ConversationActivity.NEW_CONVERSATION_TAG, true);
                startActivity(i);
                return true;
            default:

                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_conversation:
                //delete sms
                SureDeleteConversationDialog dialog = new SureDeleteConversationDialog();
                dialog.show(getFragmentManager(), "sure_delete_conv_dialog");
                return true;

            default:
                mConversationSelectedFromContextMenu = null;
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(Conversation conversation) {
        try {
            startConversationActivity(conversation);
        }catch (Exception e){
            Log.e(TAG,"Exception e:"+e);
        }
    }

    @Override
    public void onLongItemClick(ListView l,Conversation conversation){
        try {
            mConversationSelectedFromContextMenu = conversation;
            openContextMenu(l);
        }catch (Exception e){
            Log.e(TAG,"Exception e:"+e);
            mConversationSelectedFromContextMenu = null;
        }
    }

    @Override
    public void registerContextMenu(ListView l) {
        registerForContextMenu(l);
    }

    @Override
    public boolean canCompose(int position) {
        return true;
    }

    @Override
    public void onPerform(Conversation conversation) {
        try {
            startConversationActivity(conversation);
        }catch (Exception e){
            Log.e(TAG,"Exception e:"+e);
        }
    }

    /**
     * this method starts activity to show entire conversation
     * @param c to show message
     */
    public void startConversationActivity(Conversation c){
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(Conversation.ADDRESS_TAG,c.getAddress());

        intent.putExtra(Conversation.ID_TAG,c.get_id());
        intent.putExtra(Conversation.PERSON_TAG,c.getPerson());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    private void selectConversationToStart(ListView l, int position){
        ((TextView)l.getChildAt(position).findViewById(R.id.person_conversationlist_textview))
                .setTextColor(getResources().getColor(android.R.color.black));
        Conversation c = (Conversation) l.getAdapter().getItem(position);
        Log.d(TAG, "id conv: "+ c.get_id());
        startConversationActivity(c);
    }

    /**
     * this method called from context menu remove conversation
     */
    private void removeConversation(){
        if(mConversationSelectedFromContextMenu != null){

        }

        if(Utils.hasKitKat()){
            mConversationSelectedFromContextMenu.set_id(Integer.parseInt(SwipeSMSProvider.getThreadIdFromAddress(this,
                    mConversationSelectedFromContextMenu.getAddress())));
        }
        Log.d("TAG","conv id: "+mConversationSelectedFromContextMenu.get_id());
        SwipeSMSProvider.removeConversations(this,mConversationSelectedFromContextMenu.get_id());
        sendBcastToCoversationLoader();
        //reset con
        mConversationSelectedFromContextMenu = null;
    }

    /**
     * this method notify changment to list
     */
    private void sendBcastToCoversationLoader(){
        Intent i = new Intent(ConversationFragment.CONVERSATIONLIST_CHANGED);
        sendBroadcast(i);
    }

    class SureDeleteConversationDialog extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.alertdialog_remove_conv_layout, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // remove conversation
                            removeConversation();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(true);
            return builder.create();
        }
    }
}
