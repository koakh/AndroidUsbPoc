package com.koakh.usbpoc.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.koakh.usbpoc.app.Singleton;

import java.text.DecimalFormat;

/**
 * Created by mario on 05/02/2015.
 */
public class Utils {

  private static Singleton mApp = Singleton.getInstance();

  public static void log(Context pContext, String pMessage) {
    Log.i(mApp.TAG, pMessage);
    Toast.makeText(pContext, pMessage, Toast.LENGTH_SHORT).show();
  }

}
