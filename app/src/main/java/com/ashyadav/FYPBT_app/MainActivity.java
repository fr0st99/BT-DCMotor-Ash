package com.ashyadav.FYPBT_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String deviceID = null;
    private String deviceAddress;

    /* For Sensor (Shake to view) */

    private SensorManager AboutSensorMngr;
    private float Acceleration;
    private float CurrentAccel;
    private float LastAccel;
    //final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);


    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int RPM_READ = 3;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* For User Interface */

        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final Button buttonOn = findViewById(R.id.buttonOn);
        final Button buttonOff = findViewById(R.id.buttonOff);

        final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);

        final Button buttonRPM1Forward = findViewById(R.id.buttonRPM1Forward);
        final Button buttonRPM1Reverse = findViewById(R.id.buttonRPM1Reverse);

        final Button buttonRPM2Forward = findViewById(R.id.buttonRPM2Forward);
        final Button buttonRPM2Reverse = findViewById(R.id.buttonRPM2Reverse);

        final Button buttonRPM3Forward = findViewById(R.id.buttonRPM3Forward);
        final Button buttonRPM3Reverse = findViewById(R.id.buttonRPM3Reverse);

        final Button buttonRPM4Forward = findViewById(R.id.buttonRPM4Forward);
        final Button buttonRPM4Reverse = findViewById(R.id.buttonRPM4Reverse);

        final Button aboutButton = findViewById(R.id.aboutButton);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);

        SeekBar seekBarReverse = (SeekBar) findViewById(R.id.seekBarReverse);


        /* Set buttons to not visible so they can't be clicked before BT connection */

        buttonOn.setEnabled(false);
        buttonOff.setEnabled(false);
        buttonRPM1Forward.setEnabled(false);
        buttonRPM2Forward.setEnabled(false);
        buttonRPM3Forward.setEnabled(false);
        buttonRPM4Forward.setEnabled(false);
        seekBar.setEnabled(false);
        seekBarReverse.setEnabled(false);

        /* Reverse buttons */

        buttonRPM1Reverse.setEnabled(false);
        buttonRPM2Reverse.setEnabled(false);
        buttonRPM3Reverse.setEnabled(false);
        buttonRPM4Forward.setEnabled(false);
        buttonRPM4Reverse.setEnabled(false);


        // If a bluetooth device has been selected from BTConnectActivity
        deviceID = getIntent().getStringExtra("deviceName");
        if (deviceID != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceID + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {


                /* Sound effects for success and failure of BT connection - royalty free sounds from https://www.soundjay.com */

                final MediaPlayer successMP = MediaPlayer.create(MainActivity.this, R.raw.btsuccess);
                final MediaPlayer failMP = MediaPlayer.create(MainActivity.this, R.raw.btfail);


                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceID);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                buttonOn.setEnabled(true);
                                buttonOff.setEnabled(true);
                                successMP.start();

                                /* Forward button enablers */
                                buttonRPM1Forward.setEnabled(true);
                                buttonRPM2Forward.setEnabled(true);
                                buttonRPM3Forward.setEnabled(true);
                                buttonRPM4Forward.setEnabled(true);

                                /* Reverse button enablers */
                                buttonRPM1Reverse.setEnabled(true);
                                buttonRPM2Reverse.setEnabled(true);
                                buttonRPM3Reverse.setEnabled(true);
                                buttonRPM4Reverse.setEnabled(true);

                                /* Seek bar for DC motor control enabler */

                                seekBar.setEnabled(true);
                                seekBarReverse.setEnabled(true);

                                break;
                            case -1:
                                toolbar.setSubtitle("Error: Unable to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                failMP.start();
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino


                        break;
                }
            }
        };









        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, BTConnectActivity.class);
                startActivity(intent);
            }
        });



        /* Button On for DC Motor */
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String cmdText = null;
                final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
                String buttonStatus = buttonOn.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor is on and receiving power", Toast.LENGTH_SHORT).show();
                switch (buttonStatus){
                    case "turn on":

                        cmdText = "<turn on>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
                connectedThread.beginListenForData();



            }
        });





        /* Button Off for DC Motor */
        buttonOff.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonOff.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor is off", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("0");
                DirectionText.setText("Off");
                switch (buttonStatus){
                    
                    case "turn off":

                        cmdText = "<turn off>";
                        break;

                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });


        /* Button for RPM1 10,000RPM Clockwise  */
        buttonRPM1Forward.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM1Forward.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 10,000RPM in the clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("10000");
                DirectionText.setText("Forward");
                switch (buttonStatus){
                    case "forward":

                        cmdText = "<speed one forward>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        /* Button for RPM1 10,000RPM Anti-Clockwise  */
        buttonRPM1Reverse.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM1Reverse.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 10,000RPM in the Anti-Clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("10000");
                DirectionText.setText("Reverse");
                switch (buttonStatus){
                    case "reverse":

                        cmdText = "<speed one reverse>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });


        /* Button for RPM2 5,000RPM Clockwise */
        buttonRPM2Forward.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM2Forward.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 5,000RPM in the clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("5000");
                DirectionText.setText("Forward");
                switch (buttonStatus){
                    case "forward":

                        cmdText = "<speed two forward>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        /* Button for RPM2 5,000RPM Anti-Clockwise */
        buttonRPM2Reverse.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM2Reverse.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 5,000RPM in the anti-clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("5000");
                DirectionText.setText("Reverse");
                switch (buttonStatus){
                    case "reverse":

                        cmdText = "<speed two reverse>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        /* Button for RPM3 2,500RPM Clockwise */
        buttonRPM3Forward.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);

            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM3Forward.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 2,500RPM in the clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("2500");
                DirectionText.setText("Forward");
                switch (buttonStatus){
                    case "forward":

                        cmdText = "<speed three forward>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        /* Button for RPM3 2,500RPM Anti-Clockwise */
        buttonRPM3Reverse.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);

            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM3Reverse.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 2,500RPM in the anti-clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("2500");
                DirectionText.setText("Reverse");
                switch (buttonStatus){
                    case "reverse":

                        cmdText = "<speed three reverse>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });


        /* Button for RPM4 1,250RPM Clockwise */
        buttonRPM4Forward.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);

            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM4Forward.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 1,250RPM in the clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("1250");
                DirectionText.setText("Forward");
                switch (buttonStatus){
                    case "forward":

                        cmdText = "<speed four forward>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        /* Button for RPM4 1,250RPM Anti-Clockwise */
        buttonRPM4Reverse.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = (TextView) findViewById(R.id.Dir);

            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonRPM4Reverse.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor speed set to 1,250RPM in the anti-clockwise direction", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("1250");
                DirectionText.setText("Reverse");
                switch (buttonStatus){
                    case "reverse":

                        cmdText = "<speed four reverse>";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        /* About app button */
        aboutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, AboutApp.class));

            }


        });

        /* Change Value of DC motor speed by slider from 0-255 FORWARD POSITION */

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String progressValue = null;
            String cmdText = null;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Integer temp = progress;
                progressValue = temp.toString();


            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                final TextView DirectionText = (TextView) findViewById(R.id.Dir);
                final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
                /* Display motor speed value as a popup (mostly for testing purposes) */

                Toast.makeText(MainActivity.this, "DC Motor Speed Value :" + progressValue, Toast.LENGTH_SHORT).show();
                cmdText = "<speed changed>" + progressValue + "\n";
                connectedThread.write(cmdText);

                connectedThread.beginListenForData();
                //RPMDisplay.setText();

                /* Set speed and direction values into the display */

                DirectionText.setText("Forward");

            }
        });

        /* Change Value of DC motor speed by slider from 0-255 REVERSE POSITION */

        seekBarReverse.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String progressValue = null;
            String cmdText = null;

            public void onProgressChanged(SeekBar seekBarReverse, int progress, boolean fromUser) {
                Integer temp = progress;
                progressValue = temp.toString();


            }

            public void onStartTrackingTouch(SeekBar seekBarReverse) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBarReverse) {
                final TextView DirectionText = (TextView) findViewById(R.id.Dir);

                /* Display motor speed value as a popup (mostly for testing purposes) */

                Toast.makeText(MainActivity.this, "DC Motor Speed Value :" + progressValue, Toast.LENGTH_SHORT).show();
                cmdText = "<speed reverse>" + progressValue + "\n";
                connectedThread.write(cmdText);

                /* Set speed and direction values into the display */
                DirectionText.setText("Reverse");

            }
        });

        AboutSensorMngr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(AboutSensorMngr).registerListener(AboutSensorListener, AboutSensorMngr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        Acceleration = 10f;
        CurrentAccel = SensorManager.GRAVITY_EARTH;
        LastAccel = SensorManager.GRAVITY_EARTH;

    }




    /* Sensor implementation. Shake to view App author */
    /* Code adapted from my 3rd year project submitted for ED5042 */

    private final SensorEventListener AboutSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            LastAccel = CurrentAccel;

            CurrentAccel = (float) Math.sqrt((double) (x * x + y * y + z * z));

            float delta = CurrentAccel - LastAccel;

            Acceleration = Acceleration * 0.9f + delta;

            if (Acceleration > 18) {

                final MediaPlayer beepSound = MediaPlayer.create(MainActivity.this,R.raw.beep);

                final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
                final TextView DirectionText = (TextView) findViewById(R.id.Dir);
                Toast toast = Toast.makeText(getApplicationContext(), "Fall Detected! DC Motor turned off", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                RPMDisplay.setText("0");
                DirectionText.setText("Off");

                // Send command to Arduino board

                beepSound.start();
                connectedThread.write("<turn off>");

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }


    };
    @Override
    protected void onResume() {
        AboutSensorMngr.registerListener(AboutSensorListener, AboutSensorMngr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }


    @Override
    protected void onPause() {
        AboutSensorMngr.unregisterListener(AboutSensorListener);
        super.onPause();
    }




    /* Resources used for this section of code: https://examples.javacodegeeks.com/android/android-bluetooth-connection-example/ */
    /* Accessed on 15/11/2021 by Ashutosh Yadav 18249094 */
    /* Creating Bluetooth connection */
    public static class CreateConnectThread extends Thread {


        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {

            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {

                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }




    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        boolean stopThread;
        byte[] buffer = new byte[1024];


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }



       public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {

                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }


        public void beginListenForData()
        {
            final Handler handler = new Handler();
            stopThread = false;
            buffer = new byte[1024];
            Thread thread  = new Thread(() -> {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = mmInStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            mmInStream.read(rawBytes);
                            final String rpmData=new String(rawBytes,"UTF-8");
                            //final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
                            handler.post(new Runnable() {
                                public void run() {

                                    Message message = new Message();
                                    message.obj = rpmData;
                                    message.what = RPM_READ;
                                    handler.sendMessage(message);


                                    /* Test to see if values from arduino show in console */
                                    System.out.println(message);
                                    System.out.println("Message title" +RPM_READ);

                                    //RPMDisplay.setText(string);



                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            });

            thread.start();
        }


        

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
