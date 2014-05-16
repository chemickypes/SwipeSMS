package com.baraccasoftware.swipesms.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by angelo on 19/04/14.
 */
public class UnReadSMSDB {

    SQLiteDatabase mDb;
    DbHelper mDbHelper;
    Context mContext;
    private static final String DB_NAME="swipesmsdb";//nome del db
    private static final int DB_VERSION=1; //numero di versione del nostro db

    static UnReadSMSDB instance;

    public static UnReadSMSDB getInstance(Context context){
        if(instance == null) instance = new UnReadSMSDB(context);
        return instance;
    }

    private UnReadSMSDB(Context ctx){
        mContext=ctx;
        mDbHelper=new DbHelper(ctx, DB_NAME, null, DB_VERSION);
    }

    public void open(){
        mDb=mDbHelper.getWritableDatabase();

    }

    public void close(){ //chiudiamo il database su cui agiamo
        mDb.close();
    }




    public void insertUnReadSMS(String address, int not_id){ //metodo per inserire i dati
        ContentValues cv=new ContentValues();
        cv.put(UnreadSMSMetaData.ADDRESS, address);
        cv.put(UnreadSMSMetaData.NOTIFICATION_ID, not_id);
        mDb.insert(UnreadSMSMetaData.UNREAD_SMS_TABLE, null, cv);
    }

    public Cursor fetchProducts(){ //metodo per fare la query di tutti i dati
        return mDb.query(UnreadSMSMetaData.UNREAD_SMS_TABLE, null,null,null,null,null,null);
    }

    public int getIdUnReadSMS(String address){
        Cursor c = mDb.query(UnreadSMSMetaData.UNREAD_SMS_TABLE,
                new String[]{UnreadSMSMetaData.NOTIFICATION_ID},
                UnreadSMSMetaData.ADDRESS + " like '%"+ address+"%'",
                null, null, null,null);
        if(c.moveToFirst()){
            return c.getInt(0);
        }else{
            return -1;
        }
    }

    public void removeUnReadSMS(String address){
        mDb.delete(UnreadSMSMetaData.UNREAD_SMS_TABLE,
                UnreadSMSMetaData.ADDRESS + " like '%"+ address+"%'",
                null);

    }
    static class UnreadSMSMetaData {  // i metadati della tabella, accessibili ovunque
        static final String UNREAD_SMS_TABLE = "unreadsms";
        static final String ADDRESS = "address";
        static final String NOTIFICATION_ID = "notification_id";

    }

    private static final String PRODUCTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "  //codice sql di creazione della tabella
            + UnreadSMSMetaData.UNREAD_SMS_TABLE + " ("
            + UnreadSMSMetaData.ADDRESS + " text primary key , "
            + UnreadSMSMetaData.NOTIFICATION_ID + " integer not null);";

    private class DbHelper extends SQLiteOpenHelper { //classe che ci aiuta nella creazione del db

        public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(PRODUCTS_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {

        }

    }

}
