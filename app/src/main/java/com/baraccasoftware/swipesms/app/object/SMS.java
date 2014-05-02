package com.baraccasoftware.swipesms.app.object;

import java.util.Date;

/**
 * Created by angelo on 16/04/14.
 */
public class SMS {

    private int _id;
    private String address;
    private String body;
    private long date;
    private int type;
    private int status;


    public final static int STATUS_COMPLETED = 0;
    public final static int STATUS_FAILED = 64;
    public final static int STATUS_PENDING = 32;
    public final static int STATUS_NONE = -1;

    public final static int TYPE_ALL = 0;
    public final static int TYPE_INBOX = 1;
    public final static int TYPE_SENT = 2;
    public final static int TYPE_DRAFT = 3;
    public final static int TYPE_OUTBOX = 4;
    public final static int TYPE_FAILED = 5;
    public final static int TYPE_QUEUED = 6;

    public static SMS smsSentCreator(String address,String body){
        SMS sms = new SMS();
        sms.setAddress(address);
        sms.setBody(body);
        sms.setDate(new Date().getTime());
        sms.setStatus(STATUS_NONE);
        sms.setType(TYPE_OUTBOX);

        return sms;
    }

    public static SMS smsSentCreator(String address,String body,int type){
        SMS sms = new SMS();
        sms.setAddress(address);
        sms.setBody(body);
        sms.setDate(new Date().getTime());
        sms.setStatus(STATUS_NONE);
        sms.setType(type);

        return sms;
    }

    public SMS(){}

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
