package com.hardkernel.android.ODROIDRobot;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class RoutesViewer extends ListActivity {   

	private DBAdapter db;
	private WakeLock wakelock;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        
        final PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
 	   	wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
 	   	wakelock.acquire();

        db = new DBAdapter(this);
        db.open();
        Cursor cursor = db.getAllTitles();
        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.routeslist_item,
        		cursor,	new String[] { "command" , "command_date" }, new int[] { android.R.id.text1,
        		android.R.id.text2 });
        setListAdapter(adapter);
        db.close();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	db.open();
    	Cursor c = db.getTitle(l.getCount()-position);
        if (c.moveToFirst()){
        	Toast.makeText(this,
        			c.getString(1) + "\n" +
                    c.getString(2) ,
                    Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        wakelock.acquire();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakelock.release();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
  	   if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) {
             event.startTracking();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }

     public boolean onKeyUp(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
             // *** DO ACTION HERE ***
         	finish();
         	return true;
         }
         return super.onKeyUp(keyCode, event);
     }
}