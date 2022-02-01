package com.ashyadav.FYPBT_app;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

import de.nitri.gauge.Gauge;

public class MainActivity extends AppCompatActivity {




    private String deviceID = null;
    private String deviceAddress;

    /* For Sensor (Shake to view) */

    private SensorManager AboutSensorMngr;
    private float Acceleration;
    private float CurrentAccel;
    private float LastAccel;


    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status

    Gauge rpmGauge;





    protected void onCreate(Bundle savedInstanceState) {

        BroadcastReceiver msgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String rpmValue = intent.getStringExtra("theMessage");
                Log.d("receiver", "Received data  " + rpmValue);
                TextView RPMDisplay = findViewById(R.id.RPMDisplayMain);
                RPMDisplay.setText(rpmValue);

                rpmGauge = (Gauge) findViewById(R.id.gauge);

                float gaugeVal=Float.parseFloat(rpmValue);
                rpmGauge.moveToValue(gaugeVal);
            }
        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver,new IntentFilter("rpmData"));





        /* For User Interface */

        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final Button buttonOn = findViewById(R.id.buttonOn);
        final Button buttonOff = findViewById(R.id.buttonOff);
        final TextView PWMText = findViewById(R.id.PWMValue);
        final Button aboutButton = findViewById(R.id.aboutButton);


        SeekBar seekBar = findViewById(R.id.seekBar);

        SeekBar seekBarReverse = findViewById(R.id.seekBarReverse);


        /* Set buttons to not visible so they can't be clicked before BT connection */

        buttonOn.setEnabled(false);
        buttonOff.setEnabled(false);

        seekBar.setEnabled(false);
        seekBarReverse.setEnabled(false);

        /* Reverse buttons */




        // If a bluetooth device has been selected from BTConnectActivity
        deviceID = getIntent().getStringExtra("deviceName");
        if (deviceID != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceID + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);


            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {


                /* Sound effects for success and failure of BT connection - royalty free sounds from https://www.soundjay.com */

                final MediaPlayer successMP = MediaPlayer.create(MainActivity.this, R.raw.btsuccess);
                final MediaPlayer failMP = MediaPlayer.create(MainActivity.this, R.raw.btfail);


                if (msg.what == CONNECTING_STATUS) {
                    switch (msg.arg1) {
                        case 1:
                            toolbar.setSubtitle("Connected to " + deviceID);
                            progressBar.setVisibility(View.GONE);
                            buttonConnect.setEnabled(true);
                            buttonOn.setEnabled(true);
                            buttonOff.setEnabled(false);
                            successMP.start();

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
                }

            }

        };




        // Select Bluetooth Device
        buttonConnect.setOnClickListener(view -> {
            // Move to adapter list
            Intent intent = new Intent(MainActivity.this, BTConnectActivity.class);
            startActivity(intent);
        });



        /* Button On for DC Motor */
        buttonOn.setOnClickListener(view -> {

            String cmdText = null;
            String buttonStatus = buttonOn.getText().toString().toLowerCase();

            seekBar.setEnabled(true);
            seekBarReverse.setEnabled(true);
            buttonOn.setEnabled(false);
            buttonOff.setEnabled(true);


            Toast.makeText(getApplicationContext(), "The DC Motor is on and receiving power", Toast.LENGTH_SHORT).show();
            if ("turn on".equals(buttonStatus)) {
                cmdText = "<turn on>";
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);

        });





        /* Button Off for DC Motor */
        buttonOff.setOnClickListener(new View.OnClickListener() {
            final TextView RPMDisplay = findViewById(R.id.RPMDisplayMain);
            final TextView DirectionText = findViewById(R.id.Dir);
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String buttonStatus = buttonOff.getText().toString().toLowerCase();
                Toast.makeText(getApplicationContext(), "The DC Motor is off", Toast.LENGTH_SHORT).show();
                RPMDisplay.setText("0");
                DirectionText.setText("Off");
                buttonOn.setEnabled(true);
                buttonOff.setEnabled(false);

                /* Set seekbar status for both forward and reverse to 0 upon turning off */

                seekBar.setProgress(0);
                seekBar.setEnabled(false);
                seekBarReverse.setProgress(0);
                seekBarReverse.setEnabled(false);


                if ("turn off".equals(buttonStatus)) {
                    cmdText = "<turn off>";
                }
                // Send command to Arduino board
                assert cmdText != null;
                connectedThread.write(cmdText);
            }
        });




        /* About app button */
        aboutButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AboutApp.class)));

        /* Change Value of DC motor speed by slider from 0-255 FORWARD POSITION */

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String progressValue = null;
            String cmdText = null;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Integer temp = progress;
                progressValue = temp.toString();
                PWMText.setText(progressValue);


            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

                seekBarReverse.setProgress(0);

            }

            @SuppressLint("SetTextI18n")
            public void onStopTrackingTouch(SeekBar seekBar) {
                final TextView DirectionText = (TextView) findViewById(R.id.Dir);
                /* Display motor speed value as a popup (mostly for testing purposes) */

                Toast.makeText(MainActivity.this, "DC Motor Speed Value :" + progressValue, Toast.LENGTH_SHORT).show();
                cmdText = "<speed changed>" + progressValue + "\n";
                connectedThread.write(cmdText);

                /* Set speed and direction values into the display */

                DirectionText.setText("Forward");
                PWMText.setText(progressValue);

            }
        });

        /* Change Value of DC motor speed by slider from 0-255 REVERSE POSITION */

        seekBarReverse.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String progressValue = null;
            String cmdText = null;

            public void onProgressChanged(SeekBar seekBarReverse, int progress, boolean fromUser) {
                Integer temp = progress;
                progressValue = temp.toString();
                PWMText.setText(progressValue);


            }

            public void onStartTrackingTouch(SeekBar seekBarReverse) {
                // TODO Auto-generated method stub

                seekBar.setProgress(0);

            }

            @SuppressLint("SetTextI18n")
            public void onStopTrackingTouch(SeekBar seekBarReverse) {
                final TextView DirectionText = (TextView) findViewById(R.id.Dir);


                /* Display motor speed value as a popup (mostly for testing purposes) */

                Toast.makeText(MainActivity.this, "DC Motor Speed Value :" + progressValue, Toast.LENGTH_SHORT).show();

                cmdText = "<speed reverse>" + progressValue + "\n";
                connectedThread.write(cmdText);

                /* Set speed and direction values into the display */
                DirectionText.setText("Reverse");
                PWMText.setText(progressValue);


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

        @SuppressLint("SetTextI18n")
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

                SeekBar seekBar = findViewById(R.id.seekBar);

                SeekBar seekBarReverse = findViewById(R.id.seekBarReverse);

                final TextView RPMDisplay = (TextView) findViewById(R.id.RPMDisplayMain);
                final TextView DirectionText = (TextView) findViewById(R.id.Dir);
                final Button buttonOn = findViewById(R.id.buttonOn);
                final Button buttonOff = findViewById(R.id.buttonOff);


                Toast toast = Toast.makeText(getApplicationContext(), "Fall Detected! DC Motor turned off", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                RPMDisplay.setText("0");
                DirectionText.setText("Off");
                seekBar.setEnabled(false);
                seekBarReverse.setEnabled(false);
                seekBar.setProgress(0);
                seekBarReverse.setProgress(0);
                buttonOn.setEnabled(true);
                buttonOff.setEnabled(false);


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




    /* Resources used for this section of code: https://examples.javacodegeeks.com/android/android-bluetooth-connection-example/

    https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

    Credit to CodingWithMitch YouTube tutorial on threads and Bluetooth Connections https://www.youtube.com/watch?v=lwBhDnvKGd8

     */


    /* Creating Bluetooth connection */
    public class CreateConnectThread extends Thread {


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
            /* Cancel discovery otherwise connection slows down */

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {

                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {

                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }




    /* =============================== Thread for Data Transfer =========================================== */
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;



        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            /* Get the in and output streams using temps because member streams are declared as final */
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }



        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String rpmData = new String(buffer, 0, bytes);
                    Log.d(TAG, "RPMData: " + rpmData);

                    Intent rpmDataIntent = new Intent("rpmData");
                    rpmDataIntent.putExtra("theMessage", rpmData);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(rpmDataIntent);



                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
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





        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
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
