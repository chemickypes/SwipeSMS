package com.baraccasoftware.swipesms.app.util;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

import com.baraccasoftware.swipesms.app.object.Conversation;
import com.baraccasoftware.swipesms.app.object.SMS;
import com.baraccasoftware.swipesms.app.object.SwipeSMSContact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by angelo on 16/04/14.
 */
public class SwipeSMSProvider {

    private static final String TAG = "SwipeSMSProvider";
    private final static String UNREAD_SETT = "unread_settings";
    private final static String CAMPO_SAVE = "campo_salvataggio_unread";
    private static final String PREF_KEY_NOTIFICATION_ID = "notification_id";


    // first two methods are about URI
    public static Uri getThreadUri(){
        Uri uri;
        if( Utils.hasKitKat()){
            uri = Telephony.Threads.CONTENT_URI;
        }else{
            uri = Uri.parse("content://mms-sms/conversations?simple=true");
        }
        return uri;
    }

    public static Uri getSMSUri(){
        Uri uri;
        if(Utils.hasKitKat()){
            uri = Telephony.Sms.CONTENT_URI;
        }else {
            uri = Uri.parse("content://sms/");
        }
        return uri;
    }

    private static Uri getContactUri(){
        Uri uri;
        if(Utils.hasKitKat()){
            uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        }else{
            uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        }
        return uri;
    }

    private static Uri getContactForInfoUri(){
        Uri uri;
        if(Utils.hasKitKat()){
            uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        }else{
            uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI;
        }
        return uri;
    }

    public static String getThreadIdFromAddress(Context context,String address){
        Uri uri = getSMSUri();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uri,
                new String[]{Telephony.TextBasedSmsColumns.THREAD_ID},
                Telephony.TextBasedSmsColumns.ADDRESS +" = '"+ address+"'",
                null,null);
        int thread_id = -1;
        if(c.moveToFirst()) thread_id = c.getInt(0);
        Log.d(TAG,"thrad_id: "+thread_id);
        c.close();

        return ""+thread_id;
    }



    public static String getDefaultOrder(){
        String order;
        if(Utils.hasKitKat()){
            order = Telephony.Sms.Inbox.DEFAULT_SORT_ORDER;
        }else{
            order = "date DESC";
        }

        return order;
    }


    //follow methods are about unread sms
    public static int getIdUnReadSMS(Context context,String key){
        UnReadSMSDB unReadSMSDB = UnReadSMSDB.getInstance(context);
        unReadSMSDB.open();
        int i = unReadSMSDB.getIdUnReadSMS(key);
        unReadSMSDB.close();
        return i;
    }

    public static void putUnReadSMS(Context context,String key, int value){
        UnReadSMSDB unReadSMSDB = UnReadSMSDB.getInstance(context);
        unReadSMSDB.open();
        unReadSMSDB.insertUnReadSMS(key,value);
        unReadSMSDB.close();
    }

    public static void removeUnReadSMS(Context context,String key){
        UnReadSMSDB unReadSMSDB = UnReadSMSDB.getInstance(context);
        unReadSMSDB.open();
        unReadSMSDB.removeUnReadSMS(key);
        unReadSMSDB.close();
    }


    //follow method is to get all contact
    public static List<SwipeSMSContact> getAllContact(Context context){
        ArrayList<SwipeSMSContact> list = new ArrayList<SwipeSMSContact>();
        ContentResolver contentResolver = context.getContentResolver();

        Cursor c = contentResolver
                .query(getContactUri(), null, null, null,
                        ContactsContract.Contacts.DISPLAY_NAME);

        if(c.moveToFirst()){
            do{
                String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                SwipeSMSContact swipeSMSContact = new SwipeSMSContact();
                swipeSMSContact.setAddress(phoneNumber);
                swipeSMSContact.setName(name);
                list.add(swipeSMSContact);
            }while (c.moveToNext());
        }

        return list;
    }

    public static List<SwipeSMSContact> getAllContact(Context context, String search){
        ArrayList<SwipeSMSContact> list = new ArrayList<SwipeSMSContact>();
        ContentResolver contentResolver = context.getContentResolver();
        String[] v ={"'%"+search+"%'"};
        Cursor c = contentResolver
                .query(getContactUri(), null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+ " like "+ v[0]
                        + " OR "+ ContactsContract.CommonDataKinds.Phone.NUMBER + " like "+v[0], null,
                        ContactsContract.Contacts.DISPLAY_NAME);

        if(c.moveToFirst()){
            do{
                String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                SwipeSMSContact swipeSMSContact = new SwipeSMSContact();
                swipeSMSContact.setAddress(phoneNumber);
                swipeSMSContact.setName(name);
                list.add(swipeSMSContact);
            }while (c.moveToNext());
        }

        return list;
    }





    /**
     * this method return id of notification, this id is used to pending intent and unread sms too
     * @param context
     * @return
     */
    static public int getNotificationID(Context context){
        SharedPreferences prefs = context.getSharedPreferences(UNREAD_SETT,
                Context.MODE_PRIVATE);
        int notificationId = prefs.getInt(PREF_KEY_NOTIFICATION_ID, 0);
        ++notificationId;
        if (notificationId > 32765) {
            notificationId = 1;     // wrap around before it gets dangerous
        }

        // Save the updated notificationId in SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_KEY_NOTIFICATION_ID, notificationId);
        editor.apply();

        return notificationId;
    }


    //follow method are about list of conversation and sms

    public static List<Conversation> getConversationList(Context context){
        ArrayList<Conversation> list = new ArrayList<Conversation>();
        ContentResolver cr = context.getContentResolver();

        Uri uri = getThreadUri();
        Cursor c;

        if(Utils.hasKitKat()){
            c = cr.query(uri,
                    new String[]{Telephony.ThreadsColumns._ID,
                            Telephony.ThreadsColumns.DATE,
                            Telephony.ThreadsColumns._ID, //useless
                            Telephony.TextBasedSmsColumns.READ}, null, null, getDefaultOrder()
            );
        }else {


            c = cr.query(uri,
                    new String[]{Telephony.ThreadsColumns._ID,
                            Telephony.ThreadsColumns.DATE,
                            Telephony.ThreadsColumns.RECIPIENT_IDS,
                            Telephony.TextBasedSmsColumns.READ}, null, null, getDefaultOrder()
            );
        }

        list.addAll(getConversationsFromCursor(c,context));
        for (Conversation conversation:list){

            String address = "";
            if(Utils.hasKitKat()){
                String ad = getAddressFromThreadId(context, "" + conversation.get_id());
                conversation.setAddress(ad);
            }else {
                conversation.setAddress(getAddressFromRecipientID(context,conversation.getRecipient_id()));
            }

            String info[] = getContactInfoFromConversation(context,conversation);
            conversation.setPerson(info[0]);
            conversation.setPhoto(info[1]);

        }
        c.close();
        return list;
    }

    /**
     * this method return list sms searching thread_id
     * @param context
     * @param thread_id
     * @return
     */
    public static List<SMS> getSMSList(Context context, int thread_id){
        Uri uri = getSMSUri();

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(uri,
                new String[]{Telephony.BaseMmsColumns._ID,
                        Telephony.TextBasedSmsColumns.ADDRESS,
                        Telephony.TextBasedSmsColumns.BODY,
                        Telephony.TextBasedSmsColumns.DATE,
                        Telephony.TextBasedSmsColumns.TYPE,
                        Telephony.TextBasedSmsColumns.STATUS},
                Telephony.TextBasedSmsColumns.THREAD_ID+" = "+ thread_id,
                null,"_id");

        List<SMS> l = getSMSsFromCursor(cursor);
        //Log.d(TAG,"list size: "+l.size());
        cursor.close();
        return l;

    }


    /**
     * this method return list sms search address
     * @param context
     * @param address
     * @return
     */
    public static List<SMS> getSMSList(Context context,String address){
        /*Uri uri = getSMSUri();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uri,
                new String[]{Telephony.TextBasedSmsColumns.THREAD_ID},
                Telephony.TextBasedSmsColumns.ADDRESS +" = '"+ address+"'",
                null,null);
        int thread_id = -1;
        if(c.moveToFirst()) thread_id = c.getInt(0);
        Log.d(TAG,"thrad_id: "+thread_id);
        c.close();*/
        int thread_id = Integer.parseInt( getThreadIdFromAddress(context,address));
        return getSMSList(context,thread_id);

    }

    /**
     * this method ser read message all sms from conversation with trhead_id id
     * @param context
     * @param thread_id
     */

    public static boolean setReadSMS(Context context, int thread_id){
        boolean hasModified = false;
        Uri uri = getSMSUri();

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(uri,
                new String[]{Telephony.BaseMmsColumns._ID,
                        },
                "thread_id = "+ thread_id+" AND read = 0",
                null,"_id");

        if(cursor.moveToFirst()){
            hasModified = true;
            do{
                Log.d(TAG,"update sms: "+cursor.getInt(0));
                Uri u = Uri.withAppendedPath(uri,""+cursor.getInt(0));
                updateReadSMS(context,u,1);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return hasModified;
    }


    /**
     * follow methods get information, like name and photo,of person with address passed as parameter
     * @param mContext
     * @param conversation
     * @return
     */
    public static String[] getContactInfoFromConversation(Context mContext,Conversation conversation){

        return getContactInfofromAddress(mContext, conversation.getAddress());

    }

    public static String[] getContactInfofromAddress(Context mContext, String address){
        String info[] = new String[2];

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        //Uri uri = Uri.withAppendedPath(getContactForInfoUri(), Uri.encode(address));
        //Uri uri = getContactForInfoUri();
        //Log.i("URI TO LOAD NAME",uri.toString());
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME,  ContactsContract.Data.PHOTO_ID },
                null, null, null);
        if(contactLookup.moveToFirst()) {
            info[0] = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            //Log.i("ConversationUtil","name: "+name);
            info[1] = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_ID));
        }
        contactLookup.close();
        return info;
    }

    /**
     * this method get bitmap from contact
     *
     */
    public static Bitmap getPhotoContactFromConversation(Context mContext, Conversation conversation){


        return getPhotoContactFromPhotoID(mContext, conversation.getPhoto());
    }

    public static Bitmap getPhotoContactFromPhotoID(Context mContext, String photoID){
        ContentResolver cr = mContext.getContentResolver();

        byte[] photoBytes = null;
        Cursor c = null;

        try
        {
            Log.d("ID PHOTO", photoID);
            Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, Long.decode(photoID));

            c = cr.query(photoUri, new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);


            if (c.moveToFirst())
                photoBytes = c.getBlob(0);

        } catch (NullPointerException e){
            //e.printStackTrace();
            //id photo null
            Log.e("ERROR PHOTO", "id null");
        }

        catch (Exception e) {
            // TODO: handle exception
            Log.e("ERROR PHOTO", "other error");

        } finally {

            if(c != null)    c.close();
        }

        if (photoBytes != null)
            return BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        else
            Log.d("PHOTO","second try also failed");
        return null;
    }

    /**
     * this method remove sms passed as parameter
     * @param context
     * @param sms
     */
    public static void removeSMS(Context context, SMS sms){
        Uri uri = getSMSUri();
        ContentResolver cr = context.getContentResolver();
        cr.delete(Uri.withAppendedPath(uri,""+sms.get_id()),null,null);
    }

    /**
     * this method put sms to database
     * @param mContext
     * @param smsMessage message to save
     * @param type type of message
     */

    public static Uri saveSMS(Context mContext,SmsMessage smsMessage, int type, int read){
        ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("address",smsMessage.getDisplayOriginatingAddress());
        cv.put("date",smsMessage.getTimestampMillis());
        cv.put("status",smsMessage.getStatus());
        cv.put("type", type);
        cv.put("body", smsMessage.getDisplayMessageBody());
        cv.put("read",read);

        Uri uri = getSMSUri();
        return  cr.insert(uri,cv);
    }


    public static Uri saveSMS(Context context, SMS sms){
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("address",sms.getAddress());
        cv.put("date", sms.getDate());
        cv.put("status", sms.getStatus());
        cv.put("type",sms.getType());
        cv.put("body",sms.getBody());
        cv.put("read",1);//

        Uri uri = getSMSUri();

        return  cr.insert(uri,cv);
    }

    /**
     * this method updates info about sms passed as parameter
     * @param context
     * @param uri : uri of sms to update
     *
     */
    public static void updateStatusSMS(Context context, Uri uri, int status){
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        cr.update(uri,cv,null,null);
    }

    public static void updateTypeSMS(Context context,Uri uri, int type){
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("type", type);
        cr.update(uri,cv,null,null);
    }

    public static void updateReadSMS(Context context,Uri uri, int read){
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("read", read);
        cr.update(uri,cv,null,null);
    }

    /**
     * method to delete conversation
     * @param mContext context from which get content resolver
     * @param idConversation  thread_id conversation to delete
     *@return true if operations are completed, false otherwise
     */
    public static boolean removeConversations(Context mContext, int idConversation){
        // Before your loop
        ArrayList<ContentProviderOperation> operations =
                new ArrayList<ContentProviderOperation>();
        ContentResolver cr = mContext.getContentResolver();

        Uri uriSms = getSMSUri();

        Cursor cSms = cr.query(uriSms,null,"thread_id = "+idConversation,null,null);
           if(cSms.moveToFirst()){
               do{
                   operations.add(ContentProviderOperation.newDelete(
                          Uri.withAppendedPath(uriSms,""+cSms.getInt(0))).build());
               }while (cSms.moveToNext());
           }

        // After your loop
        try {
            cr.applyBatch("sms", operations); // May also try "mms-sms" in place of "sms"
            return true;
        } catch(OperationApplicationException e) {
            // Handle the error
            return false;
        } catch(RemoteException e) {
            // Handle the error
            return false;
        }
    }



    private static List<SMS> getSMSsFromCursor(Cursor cursor){
        ArrayList<SMS> list = new ArrayList<SMS>();
        if(cursor.moveToFirst()){
            do{
                SMS sms = new SMS();
                sms.set_id(cursor.getInt(0));
                sms.setAddress(cursor.getString(1));
                sms.setBody(cursor.getString(2));
                sms.setDate(Long.decode(cursor.getString(3)));
                sms.setType(cursor.getInt(4));
                sms.setStatus(cursor.getInt(5));
                list.add(sms);
            }while (cursor.moveToNext());
        }

        return list;
    }




    /**
     * this method gets info about address from recipient id
     * @param mContext
     * @param recipient_id
     * @return
     */
    private static String getAddressFromRecipientID(Context mContext,String recipient_id){
        //Log.d("CONVERSATION UTIL", recipient_id);
        Cursor c1 = mContext.getContentResolver()
                .query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id = " + recipient_id, null, null);
        String address = " ";


        if(c1.moveToFirst()) address  = c1.getString(1);
        c1.close();
        return address;
    }

    /**
     * this method get address from thread_id For kitkat
     */

    private static String getAddressFromThreadId(Context context,String thread_id){
        final Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        String address="";

        Cursor c = cr.query(Uri.withAppendedPath(uri, thread_id),
                new String[]{Telephony.Sms.Inbox.ADDRESS},
                null,
                null, null);
        if(c.moveToFirst()){
           // Log.d("ADD","dentro");
            address = c.getString(0);
        }
        c.close();
        return address;
    }

    /**
     * this method parse cursor to get every conversation from content resolver
     * @param c
     * @return
     */
    private static List<Conversation> getConversationsFromCursor(Cursor c,Context context){
        ArrayList<Conversation> list = new ArrayList<Conversation>();
        if (c.moveToFirst()) {
            do {
                //to add conversation
                Conversation conversation = new Conversation();
                conversation.set_id(Integer.parseInt(c.getString(0)));
                try {
                    conversation.setDate(Long.decode(c.getString(1)));
                }catch (NullPointerException e){
                    conversation.setDate(getDateFromThreadID(context,conversation.get_id()));
                }
                conversation.setRecipient_id(c.getString(2));
                conversation.setRead((Integer.parseInt(c.getString(3))!=0));

                //add to list
                list.add(conversation);
            } while (c.moveToNext());
        }

        return list;
    }

    /**
     * follow method is to get date when nullpointerexcpetion
     */
    private static long getDateFromThreadID(Context context, int id){
        Uri uri = getSMSUri();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uri,
                new String[]{Telephony.TextBasedSmsColumns.DATE},
                Telephony.TextBasedSmsColumns.THREAD_ID +" = "+ id,
                null,getDefaultOrder());
        long date = 00000000;
        if(c.moveToFirst()) date = c.getLong(0);
        Log.d(TAG,"date: "+date);
        c.close();
        return date;
    }



}
