package com.baraccasoftware.swipesms.app.object;

/**
 * Created by angelo on 16/04/14.
 */
public class Conversation {

    public static final String PERSON_TAG = "person_tag";
    public static final String ID_TAG = "id_tag";
    public static final String ADDRESS_TAG = "address_tag";

    private int _id;
    private String address;
    private String person;
    private String photo; //id string format
    private long date;
    private boolean read;

    private String recipient_id;

    public String getRecipient_id() {
        return recipient_id;
    }

    public void setRecipient_id(String recipient_id) {
        this.recipient_id = recipient_id;
    }

    public Conversation() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public int get_id() {

        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}
