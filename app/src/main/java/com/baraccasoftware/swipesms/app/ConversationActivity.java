package com.baraccasoftware.swipesms.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.object.SMS;
import com.baraccasoftware.swipesms.app.object.SwipeSMSContact;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ConversationActivity extends Activity implements SMSFragment.OnLongItemClickFragmentListener,
                                                    ContactFragment.OnContactChoiceListener{

    public static final String NEW_MESSAGE = "com.baraccasoftware.swipesms.new_message";
    public static final String  SENT = "com.baraccasoftware.swipesms.SMS_SENT";
    public static final String SMS_SENT_URI = "com.baraccasoftware.swipesms.SMS_SENT_URI";

    public static final String NEW_CONVERSATION_TAG = "new_conversation_tag";
    private static final String TAG = "ConversationActivity";
    private static final boolean DEBUG = false;


    //follow attr is nummber of parts of sms
    public static int partsNumber;

    SMSFragment smsFragment;
    BroadcastReceiver mNewMessageReceiver;

    private String address;
    private String person;
    private int _id;

    private EditText message;
    private ImageButton sendButton;
    private View sendMessageView; //container of element message and sendbutton

    private SMS smsContextMenuSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        this.sendMessageView = findViewById(R.id.view_send_message);
        smsFragment = new SMSFragment();
        Intent intent = getIntent();
        if(intent != null){

            if(intent.getBooleanExtra(NEW_CONVERSATION_TAG,false)){
                this.sendMessageView.setVisibility(View.GONE);
                if(DEBUG) Log.d(TAG, "new conv");
                //click + conv
                getActionBar().setTitle(getString(R.string.new_sms));
                ContactFragment fragment = new ContactFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }else{
                if(DEBUG) Log.d(TAG, "no new conv");
                //TODO: check if it's calling by another APP
                String intentAction = getIntent() == null ? null : getIntent().getAction();
                if (!TextUtils.isEmpty(intentAction) && (Intent.ACTION_SENDTO.equals(intentAction)
                        || Intent.ACTION_SEND.equals(intentAction))) {
                    //CALL FROM OTHER APP

                    Log.d("SENDTO", "Handle SEND and SENDTO intents: " + Uri.decode( getIntent().getDataString()));
                    address = Uri.decode(getIntent().getDataString().substring(6));
                    _id = -1;
                    person = SwipeSMSProvider.getContactInfofromAddress(this, address)[0];

                }else{
                //CALL FROM MAIN ACTIVITY
                    address = intent.getStringExtra(Conversation.ADDRESS_TAG);
                    person = intent.getStringExtra(Conversation.PERSON_TAG);
                    _id = intent.getIntExtra(Conversation.ID_TAG, -1);
                }


                smsFragment.setAddress(address);
                smsFragment.setThread_id(_id);
                smsFragment.setPerson(person);
                //remove possible sms
                SwipeSMSProvider.removeUnReadSMS(this,PhoneNumberUtils.stripSeparators(address));

                if(person != null){
                    getActionBar().setTitle(person);
                }else{
                    getActionBar().setTitle(address);
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, smsFragment)
                        .commit();
            }

        }




        getActionBar().setDisplayHomeAsUpEnabled(true);

        setLayout();

        mNewMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isOrderedBroadcast() && smsFragment!=null){
                    if(PhoneNumberUtils.compare(address,intent.getStringExtra(Conversation.ADDRESS_TAG))){
                        //send intent to sms loader
                        sentBroadcastToSMSLoader();
                        setResultCode(RESULT_OK);
                    }
                }
            }
        };



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.conversation, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //registro bcast
        registerReceiver(mNewMessageReceiver,new IntentFilter(NEW_MESSAGE));
        //TODO: check if deafult app for kitkat
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNewMessageReceiver != null) unregisterReceiver(mNewMessageReceiver);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_delete_sms:
                //delete sms
                SureRemoveSMSAlertDialog dialog = new SureRemoveSMSAlertDialog();
                dialog.show(getFragmentManager(),"sure_delete_sms_dialog");
                return true;
            case R.id.action_info_sms:
                //info sms
                showInfoSMS();
                smsContextMenuSelected = null;
                return true;
            default:
                smsContextMenuSelected = null;
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onLongItemClick(ListView l, int id) {
        //get sms from slected item
        smsContextMenuSelected = (SMS) l.getAdapter().getItem(id);
        openContextMenu(l);
    }

    @Override
    public void onItemClick(ListView l, int id) {
        SMS sms = (SMS)l.getAdapter().getItem(id);
        Toast.makeText(this,R.string.resending_sms,Toast.LENGTH_LONG).show();
        sendMessage(Uri.parse("content://sms/" + sms.get_id()), sms.getAddress(), sms.getBody());

    }

    @Override
    public void registerContextMenu(ListView l) {
        registerForContextMenu(l);
    }

    @Override
    public void onContactChoice(SwipeSMSContact swipeSMSContact) {
        //do something
        address = PhoneNumberUtils.stripSeparators( swipeSMSContact.getAddress());
        person = swipeSMSContact.getName();
        _id = Integer.parseInt(SwipeSMSProvider.getThreadIdFromAddress(this,address));
        smsFragment.setAddress(address);
        smsFragment.setThread_id(_id);
        smsFragment.setPerson(person);

        this.sendMessageView.setVisibility(View.VISIBLE);
        sentBroadcastToSMSLoader();
        getActionBar().setTitle(person);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, smsFragment)
                .commit();

    }

    private void setLayout(){
        final GestureDetector mGestureDetector = new GestureDetector(this,new SendSMSGestureDetector());

        message = (EditText) findViewById(R.id.message_editText);
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(message.getText())){
                    sendButton.setEnabled(false);
                }else{
                    sendButton.setEnabled(true);
                }
            }
        });

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("swipe_to_send_sms",true)) {
            //set this feature only it's settend on preference activity
            message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (mGestureDetector.onTouchEvent(motionEvent)) {
                        Log.i(TAG, "touched swipe");
                        //send sms
                        if (!TextUtils.isEmpty(message.getText())){
                            sendMessage();
                            message.setText("");
                        }
                        return true;
                    } else {
                        //Log.i(TAG,"no sms");
                        return false;
                    }

                }
            });
        }


        sendButton = (ImageButton) findViewById(R.id.message_imageButton);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                message.setText("");

            }
        });

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("swipe_to_send_sms",true)){
            sendButton.setVisibility(View.GONE);
        }else{
            sendButton.setVisibility(View.VISIBLE);
        }

        //check if we need show send button or not
    }

    /**
     * this method, called in context menu, shows the sms info, like data
     */
    private void showInfoSMS(){
        SimpleDateFormat f = new SimpleDateFormat("dd/MMM/yy HH:mm:ss");
        String type;
        if(smsContextMenuSelected.getType() == SMS.TYPE_INBOX){
            type = getString(R.string.sms_inbox);
        }else if(smsContextMenuSelected.getType() == SMS.TYPE_SENT){
            type = getString(R.string.sms_sent);
        }else if(smsContextMenuSelected.getType() == SMS.TYPE_OUTBOX){
            type = getString(R.string.sms_outbox);
        }else{
            type = getString(R.string.sms_failed);
        }

        //layout toast
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_info_sms_layout,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView textView = (TextView) layout.findViewById(R.id.layout_toast_textView);
        textView.setText(type + " in "+ f.format(smsContextMenuSelected.getDate()));

        Toast t = new Toast(this);
        t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        t.setDuration(Toast.LENGTH_LONG);
        t.setView(layout);
        t.show();



    }


    /**
     * this method called in context menu remove sms
     */
    private void removeSMS(){
        SwipeSMSProvider.removeSMS(this, smsContextMenuSelected);
        sentBroadcastToSMSLoader();
        smsContextMenuSelected=null;
    }

    /**
     * this method send sms creating uri
     */
    private void sendMessage(){

        final Uri smsUri = SwipeSMSProvider.saveSMS(this,
                SMS.smsSentCreator(PhoneNumberUtils.stripSeparators(address),message.getText().toString()));

        Log.i(TAG, "Prima : URI sms: "+ smsUri.toString());

        sendMessage(smsUri,address,message.getText().toString());

    }


    /**
     * this method send sms
     * @param uri sms uri to update
     * @param address to send sms
     * @param body sms body
     */
    private void sendMessage(Uri uri,String address, String body ){
        //sistemo intent per broadcast send
        final Intent sentI = new Intent(SENT);
        sentI.putExtra(SMS_SENT_URI, uri);
        final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                sentI,  PendingIntent.FLAG_ONE_SHOT);

        //invio broadcast to SMSLoader
        sentBroadcastToSMSLoader();

        SmsManager smsManager = SmsManager.getDefault();
        String toSend = body;
        String destination = PhoneNumberUtils.stripSeparators(address);

        if( toSend.length()<=160){
            Log.d(TAG, "one part");
            partsNumber = 1;
            smsManager.sendTextMessage(destination, null,toSend , sentPI, null);
        }else {
            ArrayList<String> parts = smsManager.divideMessage(toSend);
            partsNumber = parts.size();
            Log.d(TAG, "divided: "+partsNumber+ " part");
            ArrayList<PendingIntent> sentPIs = new ArrayList<PendingIntent>(partsNumber);
            for(int i = 0; i< parts.size();i++){

                sentPIs.add(i,sentPI);
            }

            smsManager.sendMultipartTextMessage(destination,null,parts,sentPIs,null);
        }

        //smsManager.sendTextMessage(destination, null,toSend , sentPI, null);
    }


    private void sentBroadcastToSMSLoader(){
        Intent i = new Intent(SMSFragment.NEW_SMS_CONVERSATION);
        sendBroadcast(i);
    }



    class SureRemoveSMSAlertDialog extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.alertdialog_remove_sms_layout, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // remove sms
                            removeSMS();
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

    class SendSMSGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float deltay = e1.getRawY() - e2.getRawY();
            Log.d(TAG, "deltaY: "+deltay);
            Log.d(TAG, "velocityY: "+velocityY);
            if( deltay>0 && Math.abs(velocityY) >100){
                return  true;
            }else{
                return  false;
            }

        }
    }
}
