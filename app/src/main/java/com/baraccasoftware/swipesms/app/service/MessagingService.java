/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baraccasoftware.swipesms.app.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.baraccasoftware.swipesms.app.ConversationActivity;
import com.baraccasoftware.swipesms.app.ConversationFragment;
import com.baraccasoftware.swipesms.app.R;
import com.baraccasoftware.swipesms.app.component.SMSNotification;
import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.object.SMS;
import com.baraccasoftware.swipesms.app.receiver.MessagingReceiver;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;


/**
 * This service is triggered internally only and is used to process incoming SMS and MMS messages
 * that the  passes over. It's
 * preferable to handle these in a service in case there is significant work to do which may exceed
 * the time allowed in a receiver.
 */
public class MessagingService extends IntentService {
    private static final String TAG = "MessagingService";

    // These actions are for this app only and are used by MessagingReceiver to start this service
    public static final String ACTION_MY_RECEIVE_SMS = "com.baraccasoftware.swipesms.RECEIVE_SMS";
    public static final String ACTION_MY_RECEIVE_MMS = "com.baraccasoftware.swipesms.RECEIVE_MMS";

    public MessagingService() {
        super(TAG);
    }

    private boolean DEBUG = true;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String intentAction = intent.getAction();
            if (ACTION_MY_RECEIVE_SMS.equals(intentAction)) {
                //  Handle incoming SMS
                handleSms(intent);
                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                MessagingReceiver.completeWakefulIntent(intent);
            } else if (ACTION_MY_RECEIVE_MMS.equals(intentAction)) {
                // nothing to do

                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                MessagingReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void handleSms(Intent intent) {
        final Bundle bundle = intent.getExtras();

        try {
            if(bundle != null){
                final  Object[] pdusObj = (Object[]) bundle.get("pdus");
                SmsMessage currentMessage = null;
                String bodyMessage = "";
                String addressMessage = "";
                if(DEBUG) Log.d(TAG,"size message: "+ pdusObj.length);
                for(Object currentObj : pdusObj) {
                    currentMessage = SmsMessage.createFromPdu((byte[]) currentObj);
                    bodyMessage +=  currentMessage.getDisplayMessageBody();
                    addressMessage = currentMessage.getDisplayOriginatingAddress();

                    Log.i(TAG,"Sms received from "+addressMessage + "; Text: "+bodyMessage);
                    //SwipeSMSProvider.saveSMS(this,currentMessage, SMS.TYPE_INBOX,SMS.STATUS_NONE);
                }
                final SMS sms = SMS.smsSentCreator(addressMessage,bodyMessage,SMS.TYPE_INBOX);
                SwipeSMSProvider.saveSMS(this,sms);
                /*final String address = currentMessage.getOriginatingAddress();
                final String body = currentMessage.getMessageBody();*/
                final int not_id = getNotificationID(sms.getAddress());


                //send broadcast to notify changement
                Intent i = new Intent(ConversationFragment.CONVERSATIONLIST_CHANGED);
                sendBroadcast(i);

                //send broadcast to check if smsactivity is active and it's correct
                Intent newIntent = new Intent(ConversationActivity.NEW_MESSAGE);
                if(sms != null) {
                    newIntent.putExtra(Conversation.ADDRESS_TAG, sms.getAddress());
                }
                sendOrderedBroadcast(newIntent,
                        null,
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if(getResultCode() != Activity.RESULT_OK){
                                    //send notification
                                    Log.i(TAG, "notify");
                                    //sendNotification(sms.getAddress(), sms.getBody(),not_id);
                                    SMSNotification.notify(MessagingService.this, sms.getAddress(), sms.getBody(), not_id);
                                }
                            }
                        },null,0,null,null);
            }
        }catch (Exception e){
            Log.e(TAG, "SMS error :" +e);
        }
    }

    /**
     * this method send notification
     */
    private void sendNotification(String address, String body, int not_id){
        Notification.Builder mBuilder;
        NotificationManager mNotifyManager;
        String title = getTitle(address);


        Intent i = new Intent(this, ConversationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(Conversation.ADDRESS_TAG,address);
        i.putExtra(Conversation.PERSON_TAG,title);
        i.putExtra(Conversation.ID_TAG,-1);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,
                        not_id,
                        i,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        //set element about notification
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(this);
        mBuilder.setTicker(body)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                /*.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] { 1000, 1000, 1000 })*/
                .setContentIntent(pendingIntent);

        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_ALL;
        notification.defaults |= Notification.FLAG_SHOW_LIGHTS;

        mNotifyManager.notify(not_id,notification);
    }

    /**
     * this method return id. Existent id if there ia a notification related at this address,
     * new id otherwise
     * @param address
     * @return
     */
    private int getNotificationID(String address) {
        Integer id;
        id = SwipeSMSProvider.getIdUnReadSMS(this,address);
        if(id == -1){
            id = SwipeSMSProvider.getNotificationID(this);
            SwipeSMSProvider.putUnReadSMS(this,address,id);
        }
        return id;
    }

    /**
     * this method return a title to notification:
     *   Person name if exist in device, address otherwise
     * @param address
     * @return
     */
    private String getTitle(String address){
        String person = SwipeSMSProvider.getContactInfofromAddress(this,address)[0];
        return (person == null) ? address:person;
    }
}
