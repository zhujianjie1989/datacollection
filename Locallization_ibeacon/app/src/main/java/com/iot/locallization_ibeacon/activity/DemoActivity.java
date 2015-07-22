package com.iot.locallization_ibeacon.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iot.locallization_ibeacon.R;
import com.iot.locallization_ibeacon.algorithm.WPL_Limit_BlutoothLocationAlgorithm;
import com.iot.locallization_ibeacon.pojo.GlobalData;
import com.iot.locallization_ibeacon.pojo.Node;
import com.iot.locallization_ibeacon.tools.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DemoActivity extends Activity {
    private GoogleMap map;
    private Marker currBeaconmark=null;
    private Marker currGPSmark=null;
    private boolean scan_flag= false;
    private GroundOverlay buildingMapImage =null;
    private WPL_Limit_BlutoothLocationAlgorithm location =new WPL_Limit_BlutoothLocationAlgorithm();
    private int Counter = 0;
    private int ID = 0;
    private List<Node> beaconlist = new ArrayList<Node>();
    private List<Node> gpslist = new ArrayList<Node>();
    private boolean startflag= false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        GlobalData.loghandler = updatelog ;
        initMap();
        changeBuildingMap();
        initButton();
    }

    private void changeBuildingMap()
    {
        BitmapDescriptor img =null;
        Log.e("changeBuildingMap", " floor = " + GlobalData.curr_floor);
        switch(GlobalData.curr_floor)
        {
            case 1:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k11);
                break;
            case 2:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k22);
                break;
            case 3:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k33);
                break;
            case 4:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k44);
                break;
            default:
                return;
        }
        buildingMapImage.remove();
        buildingMapImage = map.addGroundOverlay(new GroundOverlayOptions()
                .image(img).anchor(0, 0).bearing(-45f)
                .position(GlobalData.ancer, GlobalData.hw[0], GlobalData.hw[1]));
    }

    private void updateMap()
    {
        location.setHandler(updatelog);
        location.DoLocalization();
        updateLocation();

        if (currBeaconmark != null && currGPSmark!=null){
            EditText setCounter = (EditText)findViewById(R.id.ET_SETCOUNT);
            TextView TV_Counter = (TextView)findViewById(R.id.TV_Counter);

            int setcounter = Integer.parseInt(setCounter.getText().toString());
            if (!startflag || Counter >= setcounter){
                startflag = false;
                return;
            }
            Node node  = new Node(ID,Counter,GlobalData.currentPosition);
            beaconlist.add(node);

            node  = new Node(ID,Counter,Tools.locationToLatLong(GlobalData.currentGPSLocation));
            gpslist.add(node);

            Counter++;
            TV_Counter.setText(Counter + "");
        }

    }

    public void updateLocation(){

        Log.e("updateLocation","updateLocation");
        if (currBeaconmark!= null)
        {
            Log.e("updateLocation","currBeaconmark!= null");
            currBeaconmark.remove();
        }

        Log.e("updateLocation", "currBeaconmark == null");
        currBeaconmark=map.addMarker(new MarkerOptions().position(GlobalData.currentPosition).title("beacon"));
        currBeaconmark.showInfoWindow();

        if (currGPSmark!= null)
        {
            currGPSmark.remove();
        }

        if( GlobalData.currentGPSLocation!=null){
            currGPSmark=map.addMarker(new MarkerOptions().position(Tools.locationToLatLong(GlobalData.currentGPSLocation)).title("GPS"));
            currGPSmark.showInfoWindow();
        }
    }

    private void  initMap()
    {

        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setIndoorEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        buildingMapImage = map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.k44)).anchor(0,0).bearing(-45f)
                .position(GlobalData.ancer,GlobalData.hw[0],GlobalData.hw[1]));

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(GlobalData.ancer, 23);
        map.moveCamera(update);

        File file = new File(Tools.path);
        if(file.exists())
        {
            Tools.ReadConfigFile();
        }

        updateHandler.postDelayed(updateMap, 1000);
    }


    private Runnable updateMap = new Runnable()
    {
        @Override
        public void run()
        {
            updateMap();
            updateHandler.postDelayed(updateMap, 1500);
        }
    };

    Handler updateHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            updateMap();
        }
    };

    Handler updatelog = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if (msg.arg1 == 2)
            {
                changeBuildingMap();
            }

            super.handleMessage(msg);

        }
    };

    public void initButton(){
        Button start = (Button)findViewById(R.id.BT_START);
        Button seve = (Button)findViewById(R.id.BT_SAVE);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID++;
                Counter = 0;
                startflag=true;

            }
        });


        seve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Tools.writeData(beaconlist,gpslist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                beaconlist.clear();
                gpslist.clear();
            }
        });
    }





}
