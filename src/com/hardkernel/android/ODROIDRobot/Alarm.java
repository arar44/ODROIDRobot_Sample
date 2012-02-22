package com.hardkernel.android.ODROIDRobot;

import android.content.Context;
import android.media.MediaPlayer;

public class Alarm {
   private static MediaPlayer mp = null;
   private static MediaPlayer mp2 = null;
   private static MediaPlayer mp3 = null;
  
   public static void playTemperature(Context context, int resource) {
	   stopTemperature(context);
	   mp = MediaPlayer.create(context, resource);
	   mp.setAudioStreamType(3);
	   mp.setLooping(true);
	   mp.start();
   }
  
   public static void stopTemperature(Context context) { 
	   if (mp != null) {
		   mp.stop();
		   mp.release();
		   mp = null;
	   }
   }
   
   public static void playAltitude(Context context, int resource) {
	   stopAltitude(context);
	   mp2 = MediaPlayer.create(context, resource);
	   mp2.setAudioStreamType(3);
     	 mp2.setLooping(true);
     	 mp2.start();
   }
   
   public static void stopAltitude(Context context) { 
	   if (mp2 != null) {
		   mp2.stop();
		   mp2.release();
      	   mp2 = null;
	   }
   }
   
   public static void playPressure(Context context, int resource) {
	   stopPressure(context);
	   mp3 = MediaPlayer.create(context, resource);
	   mp3.setAudioStreamType(3);
	   mp3.setLooping(true);
	   mp3.start();
   }
	  
   public static void stopPressure(Context context) { 
	   if (mp3 != null) {
		   mp3.stop();
		   mp3.release();
		   mp3 = null;
	   }
   }
}
