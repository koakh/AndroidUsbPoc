package com.koakh.usbpoc.app;

import android.app.Application;

/**
 * Created by mario on 05/02/2015.
 */
public class Singleton extends Application {

  //Constants
  public final static String TAG = "USBPoc";
  //Singleton
  private static Singleton ourInstance = new Singleton();

  @Override
  public void onCreate() {
    super.onCreate();
  }

  public static Singleton getInstance() {
    return ourInstance;
  }
}