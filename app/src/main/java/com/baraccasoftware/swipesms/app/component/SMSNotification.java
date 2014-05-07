package com.baraccasoftware.swipesms.app.component;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.baraccasoftware.swipesms.app.ConversationActivity;
import com.baraccasoftware.swipesms.app.R;
import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.service.MarskAsReadIntentService;
import com.baraccasoftware.swipesms.app.util.SwipeSMSProvider;

/**
 * Created by angelo on 07/05/14.
 */
public class SMSNotification {

    private final static String TAG = "SMS Notification";
    private  final static int INIT_SUBSTRING = 0;
    private final static int END_SUBSTRING = 30;

    /**
     * this static method notifies a notification
     * @param context
     * @param address phone number
     * @param body message body
     * @param not_id id of notification
     */
    public static void notify(Context context, String address, String body, int not_id){
        final Resources res = context.getResources();
        final String ticker = body.length()> END_SUBSTRING?
                body.substring(INIT_SUBSTRING,END_SUBSTRING)+"..." : body;
        final String title = getTitle(context,address);
        final String message = body;
        final Bitmap picture = getPicture(context,address);

        //intent for click action
        Intent i = new Intent(context, ConversationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(Conversation.ADDRESS_TAG,address);
        i.putExtra(Conversation.PERSON_TAG,title);
        i.putExtra(Conversation.ID_TAG,-1);

        //intent for marck as read action
        Intent intentS = new Intent(context, MarskAsReadIntentService.class);
        intentS.setAction(MarskAsReadIntentService.ACTION_MARK_AS_READ);
        intentS.putExtra(MarskAsReadIntentService.EXTRA_ADDRESS,address);
        intentS.putExtra(MarskAsReadIntentService.EXTRA_NOT_ID,not_id);

        Notification.Builder builder = new Notification.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                        // Set required fields, including the small icon, the
                        // notification title, and text.
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)

                        // All fields below this line are optional.

                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(Notification.PRIORITY_DEFAULT)

                        // Provide a large icon, shown with the notification in the
                        // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(picture)

                        // Set ticker text (preview) information for this notification.
                .setTicker(ticker)

                        // Set the pending intent to be initiated when the user touches
                        // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(context,
                                not_id,
                                i,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                )

                        // Show expanded text content on devices running Android 4.1 or
                        // later.
                .setStyle(new Notification.BigTextStyle()
                        .bigText(message)
                        .setBigContentTitle(title))


                .addAction(
                        R.drawable.abc_ic_cab_done_holo_dark,
                        res.getString(R.string.action_mark_as_read),
                        PendingIntent.getService(context,
                                0,
                                intentS,
                                PendingIntent.FLAG_UPDATE_CURRENT))

                        // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build(),not_id);




    }


    private static void notify(final Context context, final Notification notification, int not_id) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(TAG, not_id, notification);

    }

    /**
     * Cancels any notifications of this type previously shown using
     *
     */
     public static void cancel(final Context context, int not_id) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancel(TAG, not_id);

    }

    /**
     * this method return a title to notification:
     *   Person name if exist in device, address otherwise
     * @param address
     * @return
     */
    private static String getTitle(Context context, String address){
        String person = SwipeSMSProvider.getContactInfofromAddress(context, address)[0];
        return (person == null) ? address:person;
    }

    /**
     * this method return  a bitmap to show as large icon
     */
    private static Bitmap getPicture(Context context, String address){
        String idPhoto = SwipeSMSProvider.getContactInfofromAddress(context,address)[1];
        if(idPhoto != null){
            return SwipeSMSProvider.getPhotoContactFromPhotoID(context,idPhoto);
        }else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person);
        }
    }
}
