package kenneth.jf.siaapp;

import android.app.Fragment;
import android.bluetooth.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.UUID;


/**
 * Created by User on 5/10/2016.
 */
// for beacon connection
public class secondFrag extends Fragment {
    View myView;
    // ------------------------------------------------------------------------
    // members
    // ------------------------------------------------------------------------

    private static final String LOG_TAG = "MainActivity";
    private BluetoothManager btManager;
    private android.bluetooth.BluetoothAdapter btAdapter;
    private Handler scanHandler = new Handler();
    private int scan_interval_ms = 5000;
    private boolean isScanning = false;

    // ------------------------------------------------------------------------
    // default stuff...
    // ------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.second_frag,container,false);

        // init BLE --> need to add getActivity
        btManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        scanHandler.post(scanRunnable);
        return myView;
    }

    // ------------------------------------------------------------------------
    // public usage
    // ------------------------------------------------------------------------

    private Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {

            if (isScanning)
            {
                if (btAdapter != null)
                {
                    btAdapter.stopLeScan(leScanCallback);
                }
            }
            else
            {
                if (btAdapter != null)
                {
                    btAdapter.startLeScan(leScanCallback);
                }
            }

            isScanning = !isScanning;

            scanHandler.postDelayed(this, scan_interval_ms);
        }
    };

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    private android.bluetooth.BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
        {
            int startByte = 2;
            boolean patternFound = false;
            while (startByte <= 5)
            {
                if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
                { //Identifies correct data length
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound)
            {
                //Convert to hex String
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                //UUID detection
                String uuid =  hexString.substring(0,8) + "-" +
                        hexString.substring(8,12) + "-" +
                        hexString.substring(12,16) + "-" +
                        hexString.substring(16,20) + "-" +
                        hexString.substring(20,32);

                // major
                final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

                // minor
                final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);


                //Record in front
                Log.i(LOG_TAG,"UUID: " +uuid + "\\nmajor: " +major +"\\nminor" +minor);
                setText("UUID: " +uuid + "\\nmajor: " +major +"\\nminor" +minor);

            }

        }
    };

    /**
     * bytesToHex method
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public void setText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.beaconID);
        textView.setText(text);
    }

//    protected void setAdvertiseData() {
//        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder()
//        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
//        byte[] uuid = getIdAsByte(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020"));
//        mManufacturerData.put(0, (byte)0xBE); // Beacon Identifier
//        mManufacturerData.put(1, (byte)0xAC); // Beacon Identifier
//        for (int i=2; i<=17; i++) {
//            mManufacturerData.put(i, uuid[i-2]); // adding the UUID
//        }
//        mManufacturerData.put(18, (byte)0x00); // first byte of Major
//        mManufacturerData.put(19, (byte)0x09); // second byte of Major
//        mManufacturerData.put(20, (byte)0x00); // first minor
//        mManufacturerData.put(21, (byte)0x06); // second minor
//        mManufacturerData.put(22, (byte)0xB5); // txPower
//        mBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID
//        mAdvertiseData = mBuilder.build();
//    }
//
//    protected void setAdvertiseSettings() {
//        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();
//        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
//        mBuilder.setConnectable(false);
//        mBuilder.setTimeout(0);
//        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
//        mAdvertiseSettings = mBuilder.build();
//    }
}
