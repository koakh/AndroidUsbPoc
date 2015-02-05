package com.koakh.usbpoc;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.koakh.usbpoc.app.Singleton;
import com.koakh.usbpoc.utils.Utils;


public class MainActivity extends ActionBarActivity {

  /**
   * The {@link android.support.v4.view.PagerAdapter} that will provide
   * fragments for each of the sections. We use a
   * {@link FragmentPagerAdapter} derivative, which will keep every
   * loaded fragment in memory. If this becomes too memory intensive, it
   * may be best to switch to a
   * {@link android.support.v4.app.FragmentStatePagerAdapter}.
   */
  SectionsPagerAdapter mSectionsPagerAdapter;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  ViewPager mViewPager;

  /**
   * Broadcast Receiver
   */
  private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

  /**
   * Members
   */
  private static Singleton mApp = Singleton.getInstance();
  private PendingIntent mPermissionIntent;
  private UsbManager mUsbManager;
  private HashMap<String, UsbDevice> mDeviceList;
  private UsbDevice mUsbDevice;
  private UsbInterface mUsbInterface;
  private UsbEndpoint mUsbEndpoint;
  private UsbAccessory mAccessory;
  private ParcelFileDescriptor mFileDescriptor;
  private FileInputStream mInputStream;
  private FileOutputStream mOutputStream;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Real FullScreen
    //requestWindowFeature(Window.FEATURE_NO_TITLE);
    //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.activity_main);

    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    // Set up the ViewPager with the sections adapter.
    mViewPager = (ViewPager) findViewById(R.id.pager);
    mViewPager.setAdapter(mSectionsPagerAdapter);

    //Get System Services
    mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    //RegisterBroadcastReceiver
    registerBroadcastReceiver();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    } else if (id == R.id.action_request_permission) {
      requestPermission(mUsbDevice);
      return true;
    } else if (id == R.id.action_open_accessory) {
      openAccessory();
      return true;
    } else if (id == R.id.action_get_device_list) {
      actionGetDeviceList();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
   * one of the sections/tabs/pages.
   */
  public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      // getItem is called to instantiate the fragment for the given page.
      // Return a PlaceholderFragment (defined as a static inner class below).
      return PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
      // Show 3 total pages.
      return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      Locale l = Locale.getDefault();
      switch (position) {
        case 0:
          return getString(R.string.title_section1).toUpperCase(l);
        case 1:
          return getString(R.string.title_section2).toUpperCase(l);
        case 2:
          return getString(R.string.title_section3).toUpperCase(l);
      }
      return null;
    }
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      return rootView;
    }
  }

  // =======================================================================================================================================

  private void actionGetDeviceList() {
    String deviceMessage;
    mDeviceList = mUsbManager.getDeviceList();
    Iterator<UsbDevice> deviceIterator = mDeviceList.values().iterator();
    int deviceNo = 0;
    while (deviceIterator.hasNext()) {
      //Get Device to Work On
      //UsbDevice device = deviceList.get("deviceName");
      deviceNo++;
      mUsbDevice = deviceIterator.next();
      //your code
      deviceMessage = String.format(
        "DeviceName[%d/%d]: %s, ProductId: %s, VendorId: %s",
        deviceNo,
        mDeviceList.size(),
        mUsbDevice.getDeviceName(),
        mUsbDevice.getProductId(),
        mUsbDevice.getVendorId()
      );
      Utils.log(this, deviceMessage);

      //Show Interfaces
      for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {

        mUsbInterface = mUsbDevice.getInterface(i);
        Utils.log(this, String.format("Interface DescribeContents: %d", mUsbInterface.describeContents()));

        //Request Permission: Show the Request Permission USB Dialog :)
        requestPermission(mUsbDevice);

        //Show Endpoints
        for (int j = 0; j < mUsbInterface.getEndpointCount(); j++) {
          mUsbEndpoint = mUsbInterface.getEndpoint(j);
          Utils.log(this, String.format("EndPoint DescribeContents: %d", mUsbEndpoint.describeContents()));

          //Test EndPoint
          if (mUsbDevice.getProductId() == 85 && mUsbDevice.getVendorId() == 1137) {
            testEndPoint(mUsbDevice, mUsbInterface, mUsbEndpoint);
          }
        }
      }
    }
    if (mDeviceList.size() <= 0) {
      Utils.log(this, "No Devices Found");
    }
  }

  private void requestPermission(UsbDevice pDevice) {
    mUsbManager.requestPermission(pDevice, mPermissionIntent);
  }

  private void testEndPoint(UsbDevice pUsbDevice, UsbInterface pUsbInterface, UsbEndpoint pUsbEndpoint) {

    String stringData = "";
    //stringData = stringData + "\\x1b\\x40"; // esc @ (init)
    //stringData = stringData + "\\x0a";// line feed
    //stringData = stringData + "\\x0a";// line feed
    stringData = stringData + "Hello World";// text
    //stringData = stringData + "\\x0a";// line feed
    //stringData = stringData + "\\x1d\\x56\\x42\\x03";// #cut the paper
    final byte[] bytesData1 = stringData.getBytes(Charset.defaultCharset());

    final byte[] bytesData2 = new byte[16];
    //esc @ init
    bytesData2[0] = 0x1B;
    bytesData2[1] = 0x40;
    //Hello
    bytesData2[2] = "H".getBytes()[0];
    bytesData2[3] = "e".getBytes()[0];
    bytesData2[4] = "l".getBytes()[0];
    bytesData2[5] = "l".getBytes()[0];
    bytesData2[6] = "o".getBytes()[0];
    //LineFeed
    bytesData2[7] = 0x0A;
    bytesData2[8] = 0x0A;
    bytesData2[9] = 0x0A;
    bytesData2[10] = 0x0A;
    bytesData2[11] = 0x0A;
    //cut the paper
    bytesData2[12] = 0x1D;
    bytesData2[13] = 0x56;
    bytesData2[14] = 0x42;
    bytesData2[15] = 0x03;

    //TODO: Move to Settings
    final int TIMEOUT = 0;
    boolean forceClaim = true;

    try {
      //Variable is accessed within inner class. Needs to be declared final
      //http://stackoverflow.com/questions/14425826/variable-is-accessed-within-inner-class-needs-to-be-declared-final
      final UsbEndpoint usbEndpoint = pUsbEndpoint;
      final UsbDeviceConnection connection = mUsbManager.openDevice(pUsbDevice);
      connection.claimInterface(pUsbInterface, forceClaim);

      new Handler().postDelayed(new Runnable() {
        public void run() {
          connection.bulkTransfer(usbEndpoint, bytesData2, bytesData2.length, TIMEOUT); //do in another thread
        }
      }, 100);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void openAccessory() {
    Utils.log(this, "openAccessory: " + mAccessory);

    mFileDescriptor = mUsbManager.openAccessory(mAccessory);
    if (mFileDescriptor != null) {
      FileDescriptor fd = mFileDescriptor.getFileDescriptor();
      mInputStream = new FileInputStream(fd);
      mOutputStream = new FileOutputStream(fd);
//Thread thread = new Thread(null, this, "AccessoryThread");
//thread.start();
    }
  }

  private void setupUsbCommunication() {
  }

  /**
   * BroadcastReceiver
   */
  private void registerBroadcastReceiver() {
    UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    //Setup PendingIntent
    mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    //Register BroadcastRecevive
    registerReceiver(mUsbReceiver, filter);
  }

  /**
   * When users reply to the dialog, your broadcast receiver receives the intent that contains the
   * EXTRA_PERMISSION_GRANTED extra, which is a boolean representing the answer. Check this extra for a value
   * of true before connecting to the accessory.
   */
  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

          //Toast.makeText(context, "BroadcastReceiver Accessory: " + accessory.getDescription(), Toast.LENGTH_SHORT).show();

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (accessory != null) {
              //call method to set up accessory communication
              //setupUsbCommunication();
mAccessory = accessory;
            }
          } else {
            Utils.log(context, "permission denied for accessory " + accessory);
          }
        }
      }
    }
  };

}
