package com.hardkernel.android.ODROIDRobot;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class Settings extends PreferenceActivity{

	private WakeLock wakelock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		addPreferencesFromResource(R.xml.settings);
		
		final PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
 	   	wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
 	   	wakelock.acquire();
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
