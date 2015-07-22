package com.iot.locallization_ibeacon.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.iot.locallization_ibeacon.pojo.CallBack;
import com.iot.locallization_ibeacon.pojo.GlobalData;
import com.iot.locallization_ibeacon.tools.Tools;

import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {
    private TimerTask task;
    private Boolean on_off = false;
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager;
    private  static Timer timer = null;
    private BluetoothAdapter.LeScanCallback leScanCallback = new CallBack();

    @Override
    public IBinder onBind(Intent intent) {
        initBlueTooth();
        initTask();
        openGPSSettings();
        return  new Binder();
    }


    private void  initBlueTooth()  {

        try
        {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
            {
                throw new Exception("Bluetooth is not available");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public void initTask(){

        task = new TimerTask()
        {
            @Override
            public void run()
            {
                on_off = !on_off;
                if (on_off)
                {
                    bluetoothAdapter.startLeScan(leScanCallback);
                    Log.e("mBluetoothAdapter","startLeScan");
                }
                else
                {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    Log.e("mBluetoothAdapter", "stopLeScan");
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, 1000, 800);
    }

    public static  void stopTimer()
    {
        timer.cancel();
    }


    private void openGPSSettings() {

        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 0, GPSlistener);
            Toast.makeText(this, "GPS is ok£¡", Toast.LENGTH_LONG).show();
            Log.e("GPS is ok£¡", "GPS is ok£¡");
            return;
        }

        Log.e("Çë¿ªÆôGPS£¡", "Çë¿ªÆôGPS£¡");
        Toast.makeText(this, "Çë¿ªÆôGPS£¡", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);

    }

    LocationListener GPSlistener =  new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            GlobalData.currentGPSLocation = location;
            if( GlobalData.currentGPSLocation!=null)
            {
                if(Tools.isBetterLocation(location,  GlobalData.currentGPSLocation))
                {
                    Log.v("GPSTEST", "It's a better location");
                    GlobalData.currentGPSLocation = location;
                }
                else
                {
                    Log.v("GPSTEST", "Not very good!");
                }
            }
            else if(location.getAccuracy() < 5)
            {
                Log.v("GPSTEST", "It's first location");
                GlobalData.currentGPSLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
