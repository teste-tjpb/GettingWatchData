package com.fpmd.wearhealth.modal;

/**
 * Created by vikasaggarwal on 01/04/18.
 */

public class WatchData {

    int id,type;
    String response,  unique_id,  time_stamp;


    public WatchData() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public WatchData(int id, int type, String response, String unique_id, String time_stamp) {
        this.id = id;
this.type = type;

        this.response = response;
        this.unique_id = unique_id;
        this.time_stamp = time_stamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public void setUnique_id(String unique_id) {
        this.unique_id = unique_id;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }
}
