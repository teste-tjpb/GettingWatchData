package com.fpmd.wearhealth.modal;

/**
 * Created by vikasaggarwal on 01/04/18.
 */

public class WatchData_New {


    private int id;
    private String type;
    private int month;
    private int year;
    private int hour;
    private int min;
    private int steps;


    private int calorie;
    private String km;
    private String text_heart_rate;
    private String text_BP_O;


    public WatchData_New(int id, String type, int month, int year, int hour, int min, int steps, int calorie, String km, String text_heart_rate, String text_BP_O) {
        this.id = id;
        this.type = type;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.min = min;
        this.steps = steps;
        this.calorie = calorie;
        this.km = km;
        this.text_heart_rate = text_heart_rate;
        this.text_BP_O = text_BP_O;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getText_heart_rate() {
        return text_heart_rate;
    }

    public void setText_heart_rate(String text_heart_rate) {
        this.text_heart_rate = text_heart_rate;
    }

    public String getText_BP_O() {
        return text_BP_O;
    }

    public void setText_BP_O(String text_BP_O) {
        this.text_BP_O = text_BP_O;
    }
}
