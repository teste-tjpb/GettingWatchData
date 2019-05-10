package com.fpmd.wearhealth.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.fpmd.wearhealth.Utility.UtilityFunctions;
import com.fpmd.wearhealth.modal.WatchData;

/**
 * Created by vikasaggarwal on 03/04/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "watch_database";


    public static final String TABLE_NAME = "smart_watch";
    public static final String COLUMN_PRIMARY_ID = "id";
    private static final String COLUMN_RESPONSE = "response";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_UNIQUE_ID = "unique_id";
    private static final String COLUMN_TIME_STAMP = "COLUMN_TIME_STAMP";

    private String response;
    private  String unique_id;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_RESPONSE + " TEXT,"
                    + COLUMN_TYPE + " INTEGER,"
                    + COLUMN_UNIQUE_ID + " TEXT NOT NULL UNIQUE,"
                    + COLUMN_TIME_STAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
// create watch table
        sqLiteDatabase.execSQL(CREATE_TABLE);
        UtilityFunctions.showLogErrorStatic("Database Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public long insertNote(String response,String unique_id ,int type) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(COLUMN_RESPONSE, response);
        values.put(COLUMN_UNIQUE_ID, unique_id);
        values.put(COLUMN_TYPE, type);

        // insert row
        long id;
        try {
           id = db.insert(TABLE_NAME, null, values);
        }catch (Exception e){
            id = 0;
        }
        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public WatchData getNote(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_PRIMARY_ID,COLUMN_TYPE, COLUMN_RESPONSE, COLUMN_UNIQUE_ID,COLUMN_TIME_STAMP},
                COLUMN_PRIMARY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        WatchData note = new WatchData(
                cursor.getInt(cursor.getColumnIndex(COLUMN_PRIMARY_ID)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_UNIQUE_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TIME_STAMP))
        );

        // close the db connection
        cursor.close();

        return note;
    }

    public List<WatchData> getAllNotes() {
        List<WatchData> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " +
                COLUMN_TIME_STAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                WatchData note = new WatchData();
                note.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRIMARY_ID)));
                note.setType(cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE)));
                note.setResponse(cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE)));
                note.setUnique_id(cursor.getString(cursor.getColumnIndex(COLUMN_UNIQUE_ID)));
                note.setTime_stamp(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_STAMP)));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

   /* public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Note.COLUMN_NOTE, note.getNote());

        // updating row
        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }*/

    public void deleteNote(WatchData note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PRIMARY_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,null,null);
        db.close();
    }
}
