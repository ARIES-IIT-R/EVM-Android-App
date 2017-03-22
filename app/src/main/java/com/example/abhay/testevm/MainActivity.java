/*
Android Example to connect to and communicate with Bluetooth
In this exercise, the target is a Arduino Due + HC-06 (Bluetooth Module)

Ref:
- Make BlueTooth connection between Android devices
http://android-er.blogspot.com/2014/12/make-bluetooth-connection-between.html
- Bluetooth communication between Android devices
http://android-er.blogspot.com/2014/12/bluetooth-communication-between-android.html
 */
package com.example.androidbtcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhay.testevm.candidate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;

    ArrayList<BluetoothDevice> pairedDeviceArrayList;

    EditText input_no,input_names;
    Button enter,enter2;
    ArrayList<String> ar = new ArrayList<String>();
    int n,i=0;
    String name,s;

    TextView textInfo,textStatus,votingstatus;
    ListView listViewPairedDevice;
    LinearLayout inputPane, inputno, inputgone;
    //EditText inputField;
    Button btnSend;
    Button rstSend;
    Button electionSend;
    Button voting;
    EditText inputField;
    boolean voting_enable;
    ArrayList<String> voter = new ArrayList<String>();
    int size=0;

    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ar.add("Project 1");ar.add("Project 2");ar.add("Project 3");ar.add("Project 4");ar.add("Project 5");
        ar.add("Project 6");ar.add("Project 7");ar.add("Project 8");ar.add("Project 9");ar.add("Project 10");
        s = ar.get(0) + ":";
        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);

        inputPane = (LinearLayout)findViewById(R.id.inputpane);
        inputno = (LinearLayout)findViewById(R.id.inputno);
        inputgone = (LinearLayout)findViewById(R.id.inputgone);

        input_no = (EditText)findViewById(R.id.candidate_no);
        input_names = (EditText)findViewById(R.id.candidate_name);
        enter= (Button)findViewById(R.id.enter);
        enter2 = (Button)findViewById(R.id.enter2);
        //inputField = (EditText)findViewById(R.id.input);
        btnSend = (Button)findViewById(R.id.send);
        btnSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(myThreadConnected!=null){
                    String result = "r";
                    byte[] bytesToSend = result.getBytes();
                    myThreadConnected.write(bytesToSend);
                }
            }});

        /* to start new election */
        electionSend = (Button)findViewById(R.id.election);
        electionSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                inputPane.setVisibility(View.GONE);
                inputno.setVisibility(View.VISIBLE);
            }});
        voting = (Button)findViewById(R.id.vote);
        inputField = (EditText)findViewById(R.id.input);
        votingstatus = (TextView)findViewById(R.id.voting_status);
        voting.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String new_voter= inputField.getText().toString();
                voting_enable = true;
                for(int i=0; i<voter.size(); i++)
                {
                    if(new_voter.equals(voter.get(i)))
                    {
                        voting_enable = false;
                        votingstatus.setText("Access Denied!! Already Voted...");
                        break;
                    }
                }
                if(voting_enable==true)
                {
                    votingstatus.setText("Voting Enabled...");
                    voter.add(new_voter);
                    String result = "t";
                    byte[] bytesToSend = result.getBytes();
                    myThreadConnected.write(bytesToSend);
                }
            }});

        rstSend = (Button)findViewById(R.id.rst);
        rstSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(myThreadConnected!=null){
                    String result = "f";
                    byte[] bytesToSend = result.getBytes();
                    myThreadConnected.write(bytesToSend);
                }
            }});

        /* for entering new candidates */
        enter.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                n = Integer.parseInt(input_no.getText().toString());
                ar.clear();
                if(myThreadConnected!=null){
                    String result = Integer.toString(n-1);
                    byte[] bytesToSend = result.getBytes();
                    myThreadConnected.write(bytesToSend);
                }
                voter.clear();
                textStatus.setText("");
                votingstatus.setText("");
                inputgone.setVisibility(View.VISIBLE);
            }});

        enter2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                name = input_names.getText().toString();
                ar.add(name);
                i++;
                if(i==n)
                {
                    s=ar.get(0) + ":";
                    input_no.setText("");
                    input_names.setText("");
                    inputno.setVisibility(View.GONE);
                    inputPane.setVisibility(View.VISIBLE);
                }
                else
                {
                    input_names.setText("");
                }
            }});



        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        textInfo.setText(stInfo);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
            }

            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
                    android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    BluetoothDevice device =
                            (BluetoothDevice) parent.getItemAtPosition(position);
                    Toast.makeText(MainActivity.this,
                            "Name: " + device.getName() + "\n"
                                    + "Address: " + device.getAddress() + "\n"
                                    + "BondState: " + device.getBondState() + "\n"
                                    + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                                    + "Class: " + device.getClass(),
                            Toast.LENGTH_LONG).show();

                    textStatus.setText("start ThreadConnectBTdevice");
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                setup();
            }else{
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("Something went wrong...");
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "Connection Successful:\n";

                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        textStatus.setText(msgconnected);

                        listViewPairedDevice.setVisibility(View.GONE);
                        inputPane.setVisibility(View.VISIBLE);
                    }});

                startThreadConnected(bluetoothSocket);
            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            int i=1;
            //String s=ar.get(0) + ":";
            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                        for (int j = 0; j < strReceived.length(); j++) {
                            if (strReceived.charAt(j) == '#') {
                                s += "\n" + ar.get(i) + ":";
                                i++;
                            }
                            else if (strReceived.charAt(j) == '~') {
                                final String msgReceived = s;

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        textStatus.setText(msgReceived);
                                    }
                                });
                                s = ar.get(0) + ":";
                                i = 1;
                            }
                            else
                            {
                                s += strReceived.charAt(j);
                            }
                        }
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
