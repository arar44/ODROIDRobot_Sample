package com.hardkernel.android.ODROIDRobot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_COMMAND = "command";
	public static final String KEY_DATE = "command_date";  
    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "routes";
    private static final String DATABASE_TABLE = "command";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
        "create table command (_id integer primary key autoincrement, "
        + KEY_COMMAND + " text not null, "+ KEY_DATE +" text not null);";
        
    private final Context context; 
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    public DBAdapter(Context mContext){
        this.context = mContext;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper {
    	
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DATABASE_CREATE); 
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion){
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }    
    
    //---opens the database---
    public DBAdapter open() throws SQLException{
        db = DBHelper.getWritableDatabase();
        return this;
    }
    
    //---closes the database---    
    public void close(){
        DBHelper.close();
    }
    
    //---insert a title into the database---
    public long insertTitle(String command, String date){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_COMMAND, command);
        initialValues.put(KEY_DATE, date);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }
    
    //---deletes a particular title---
    public boolean deleteTitle(long rowId){
        return db.delete(DATABASE_TABLE, KEY_ROWID + 
         "=" + rowId, null) > 0;
    }
    
    //---retrieves all the titles---
    public Cursor getAllTitles(){
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_COMMAND, KEY_DATE}, 
        		null, null, null, null, KEY_DATE +" DESC");
    }
    
    //---retrieves a particular title---
    public Cursor getTitle(long rowId) throws SQLException{
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_COMMAND,
        		KEY_DATE}, KEY_ROWID + "=" + rowId, null, null, null, KEY_DATE +" DESC", 
        		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    //---updates a title---
    public boolean updateTitle(long rowId, String command, String date){
        ContentValues args = new ContentValues();
        args.put(KEY_COMMAND, command);
        args.put(KEY_DATE, date);
        return db.update(DATABASE_TABLE, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
}