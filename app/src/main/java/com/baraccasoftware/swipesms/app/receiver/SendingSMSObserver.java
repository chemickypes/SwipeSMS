package com.baraccasoftware.swipesms.app.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.baraccasoftware.swipesms.app.ConversationActivity;
import com.baraccasoftware.swipesms.app.ConversationFragment;
import com.baraccasoftware.swipesms.app.SMSFragment;
import com.baraccasoftware.swipesms.app.object.SMS;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;

/**
 * Created by angelo on 17/04/14.
 */
public class SendingSMSObserver extends BroadcastReceiver {
    private static final String TAG = "SendingSMSObserver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri uri = (Uri) intent.getExtras().getParcelable(ConversationActivity.SMS_SENT_URI);

        Log.i(TAG, "URI sms: " + uri.toString());


        switch (getResultCode()) {
            case Activity.RESULT_OK:
                        /*Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();*/

                SwipeSMSProvider.updateTypeSMS(context, uri, SMS.TYPE_SENT);


                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        /*Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;*/
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                        /*Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;*/
            case SmsManager.RESULT_ERROR_NULL_PDU:
                        /*Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;*/
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                        /*Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();*/
                SwipeSMSProvider.updateTypeSMS(context,uri,SMS.TYPE_FAILED);
                break;
        }

        sentBroadcastToSMSLoader(context);
        //send this intet to notify that conversation are changed
        sendBroadcastToConversationLoader(context);


    }

    private void sendBroadcastToConversationLoader(Context context) {
        Intent i = new Intent(ConversationFragment.CONVERSATIONLIST_CHANGED);
        context.sendBroadcast(i);
    }

    private void sentBroadcastToSMSLoader(Context context) {
        Intent i = new Intent(SMSFragment.NEW_SMS_CONVERSATION);
        context.sendBroadcast(i);
    }
}
