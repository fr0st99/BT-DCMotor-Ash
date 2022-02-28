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
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import de.nitri.gauge.Gauge;

public class MainActivity extends AppCompatActivity {

    /* For welcome text */

    public TextView userNameText;
    private Button toggleTheme;

    /* For Timer */

    private EditText inputTime;
    private TextView countDownText;
    private Button setTimeButton;
    private Button startPauseTimeButton;
    private Button resetTimeButton;
    private CountDownTimer myCountDownTimer;
    private boolean countDownTimerRunning;
    private long c_StartTimeInMillis;
    private long c_TimeLeftInMillis;
    private long c_EndTime;

    /* For Device Address */


    private String deviceID = null;
    private String deviceAddress;

    /* For Sensor (Shake to view) */

    private SensorManager AboutSensorMngr;
    private float Acceleration;
    private float CurrentAccel;
    private float LastAccel;
    Gauge rpmGauge;


    /* Handler */

    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status

    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // prevent timeout or screen off during operation


        BroadcastReceiver msgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String rpmValue = intent.getStringExtra("RPM Data");
                Log.d("receiver", "Received data  " + rpmValue);
                TextView RPMDisplay = findViewById(R.id.RPMDisplayMain);
                TextView freqValue = findViewById(R.id.freqVal);
                RPMDisplay.setText(rpmValue);
                float rpmInt = Integer.parseInt(rpmValue.trim());
                float freq = (rpmInt / 60);
                double freqRounded = Math.round(freq * 100.0) / 100.0;
                String freqString = String.valueOf(freqRounded);
                freqValue.setText(freqString +"Hz");


                rpmGauge = findViewById(R.id.gauge);

                float gaugeVal = Float.parseFloat(rpmValue);
                rpmGauge.moveToValue(gaugeVal);
            }
        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, new IntentFilter("rpmData"));


        /* Welcome Text */

        userNameText = findViewById(R.id.userNameText);
        SharedPreferences results = getSharedPreferences("username", Context.MODE_PRIVATE);
        String value = results.getString("Value", "No username found");
        userNameText.setText(value);
        ToggleButton darkModeToggle = findViewById(R.id.themeToggle);


        /* Timer views */

        inputTime = findViewById(R.id.edit_text_input);
        countDownText = findViewById(R.id.text_view_countdown);
        setTimeButton = findViewById(R.id.button_set);
        startPauseTimeButton = findViewById(R.id.button_start_pause);
        resetTimeButton = findViewById(R.id.button_reset);

        /* For User Interface */

        final ImageButton connectIcon = findViewById(R.id.connectIcon);
        final ImageButton disconnectIcon = findViewById(R.id.disconnectIcon);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final Button buttonOn = findViewById(R.id.buttonOn);
        final Button buttonOff = findViewById(R.id.buttonOff);
        final TextView PWMText = findViewById(R.id.PWMValue);
        SeekBar seekBar = findViewById(R.id.seekBar);
        SeekBar seekBarReverse = findViewById(R.id.seekBarReverse);



        /* Set buttons to not visible so they can't be clicked before BT connection */

        buttonOn.setEnabled(false);
        buttonOff.setEnabled(false);
        inputTime.setEnabled(false);
        resetTimeButton.setEnabled(false);
        setTimeButton.setEnabled(false);
        startPauseTimeButton.setEnabled(false);
        seekBar.setEnabled(false);
        seekBarReverse.setEnabled(false);
        disconnectIcon.setEnabled(false);
        ((ImageButton) findViewById(R.id.disconnectIcon)).setImageAlpha(0x3F);
        darkModeToggle.setEnabled(true);
        darkModeToggle.setAlpha(1.0F);

        /**************************************Dark Mode Toggle *************************************************/

        /* Thanks to https://www.geeksforgeeks.org/how-to-implement-dark-night-mode-in-android-app/ for tutorial on using shared preferences for storing dark mode setting */


        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean checkDarkMode = sharedPreferences.getBoolean("checkDarkMode", false);

        if (checkDarkMode) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            // Light Mode
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            //Dark Mode
        }

        darkModeToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (checkDarkMode) {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("checkDarkMode", false);
                    editor.apply();


                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


                    editor.putBoolean("checkDarkMode", true);
                    editor.apply();

                }
            }
        });


        // If a bluetooth device has been selected from BTConnectActivity
        deviceID = getIntent().getStringExtra("deviceName");
        if (deviceID != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceID + "...");
            progressBar.setVisibility(View.VISIBLE);
            //connectIcon.setEnabled(false);


            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {

                /* Sound effects for success and failure of BT connection - royalty free sounds from https://www.soundjay.com */

                final MediaPlayer successMP = MediaPlayer.create(MainActivity.this, R.raw.connected);
                final MediaPlayer failMP = MediaPlayer.create(MainActivity.this, R.raw.btfail);

                if (msg.what == CONNECTING_STATUS) {
                    switch (msg.arg1) {
                        case 1:
                            toolbar.setSubtitle("Connected to " + deviceID);
                            progressBar.setVisibility(View.GONE);
                            ((ImageButton) findViewById(R.id.connectIcon)).setImageAlpha(0x3F);
                            ((ImageButton) findViewById(R.id.disconnectIcon)).setImageAlpha(0xFF);
                            disconnectIcon.setEnabled(true);
                            buttonOn.setEnabled(true);
                            buttonOff.setEnabled(false);
                            connectIcon.setEnabled(false);

                            darkModeToggle.setEnabled(false);
                            darkModeToggle.setAlpha(.5f);

                            successMP.start();

                            /* Seek bar for DC motor control enabler */

                            seekBar.setEnabled(false);
                            seekBarReverse.setEnabled(false);

                            /* Enable text field for timer */

                            inputTime.setEnabled(false);
                            setTimeButton.setEnabled(false);
                            startPauseTimeButton.setEnabled(false);
                            resetTimeButton.setEnabled(false);

                            break;
                        case -1:
                            toolbar.setSubtitle("Error: Unable to connect");
                            progressBar.setVisibility(View.GONE);
                            connectIcon.setEnabled(true);
                            failMP.start();
                            darkModeToggle.setEnabled(true);
                            darkModeToggle.setAlpha(1.0F);
                            break;
                    }
                }

            }

        };


        /* Connect to BT device */

        connectIcon.setOnClickListener(view -> {
            // Move to adapter list
            Intent intent = new Intent(MainActivity.this, BTConnectActivity.class);
            startActivity(intent);
            ((ImageButton) findViewById(R.id.connectIcon)).setImageAlpha(0x3F);
            darkModeToggle.setEnabled(true);
            darkModeToggle.setAlpha(1.0F);



        });

        /* Disconnect from currently connected BT device */

        disconnectIcon.setOnClickListener(view -> {

            final MediaPlayer disconnected = MediaPlayer.create(MainActivity.this, R.raw.disconnected);

            createConnectThread.cancel();
            toolbar.setSubtitle("");
            Toast.makeText(getApplicationContext(), "Disconnected from " +deviceID, Toast.LENGTH_SHORT).show();
            inputTime.setEnabled(false);
            setTimeButton.setEnabled(false);
            startPauseTimeButton.setEnabled(false);
            resetTimeButton.setEnabled(false);
            buttonOn.setEnabled(false);
            seekBar.setProgress(0);
            seekBar.setEnabled(false);
            seekBarReverse.setProgress(0);
            seekBarReverse.setEnabled(false);
            darkModeToggle.setEnabled(true);
            darkModeToggle.setAlpha(1.0F);
            disconnected.start();

            cancelTimer();
            resetTimer();





            ((ImageButton) findViewById(R.id.disconnectIcon)).setImageAlpha(0x3F);
            ((ImageButton) findViewById(R.id.connectIcon)).setImageAlpha(0xFF);


        });



        /* Button On for DC Motor */
        buttonOn.setOnClickListener(view -> {

            String cmdText = null;
            String buttonStatus = buttonOn.getText().toString().toLowerCase();

            seekBar.setEnabled(true);
            seekBarReverse.setEnabled(true);
            buttonOn.setEnabled(false);
            buttonOff.setEnabled(true);

            /* Turn on timer controls */

            inputTime.setEnabled(true);
            resetTimeButton.setEnabled(true);
            setTimeButton.setEnabled(true);
            startPauseTimeButton.setEnabled(true);
            ((ImageButton) findViewById(R.id.disconnectIcon)).setImageAlpha(0x3F);
            disconnectIcon.setEnabled(false);
            connectIcon.setEnabled(false);

            /* Dark Mode controls */

            darkModeToggle.setEnabled(false);
            darkModeToggle.setAlpha(.5f);






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

                ((ImageButton) findViewById(R.id.disconnectIcon)).setImageAlpha(0xFF );
                disconnectIcon.setEnabled(true);
                connectIcon.setEnabled(false);

                /* Set seekbar status for both forward and reverse to 0 upon turning off */

                seekBar.setProgress(0);
                seekBar.setEnabled(false);
                seekBarReverse.setProgress(0);
                seekBarReverse.setEnabled(false);

                /* Turn off timer controls */

                inputTime.setEnabled(false);
                resetTimeButton.setEnabled(false);
                setTimeButton.setEnabled(false);
                startPauseTimeButton.setEnabled(false);


                if ("turn off".equals(buttonStatus)) {
                    cmdText = "<turn off>";
                }
                // Send command to Arduino board
                assert cmdText != null;
                connectedThread.write(cmdText);
                cancelTimer();
                resetTimer();

            }
        });



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
                final TextView DirectionText = findViewById(R.id.Dir);
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
                final TextView DirectionText = findViewById(R.id.Dir);


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



        /**************************************Countdown Timer *************************************************/


        setTimeButton.setOnClickListener(v -> {
            String input = inputTime.getText().toString();
            if (input.length() == 0) {
                Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            long millisInput = Long.parseLong(input) * 60000;
            if (millisInput == 0) {
                Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                return;
            }

            setTime(millisInput);
            inputTime.setText("");
        });


        startPauseTimeButton.setOnClickListener(v -> {
            if (countDownTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        resetTimeButton.setOnClickListener(v -> resetTimer());




    }


    /**************************************END OF ON CREATE *************************************************/


    /* Timer Methods */


    private void setTime(long milliseconds) {
        c_StartTimeInMillis = milliseconds;
        resetTimer();

    }

    private void startTimer() {

        /* Declarations */

        final TextView DirectionText = findViewById(R.id.Dir);
        final MediaPlayer beepSound = MediaPlayer.create(MainActivity.this,R.raw.beep);
        final TextView RPMDisplay = findViewById(R.id.RPMDisplayMain);
        final Button buttonOn = findViewById(R.id.buttonOn);
        final Button buttonOff = findViewById(R.id.buttonOff);
        SeekBar seekBar = findViewById(R.id.seekBar);
        SeekBar seekBarReverse = findViewById(R.id.seekBarReverse);
        final ImageButton disconnectIcon = findViewById(R.id.disconnectIcon);


        c_EndTime = System.currentTimeMillis() + c_TimeLeftInMillis;

        myCountDownTimer = new CountDownTimer(c_TimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                c_TimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {


                /* Direction Text and controls */

                DirectionText.setText("Off");
                seekBar.setProgress(0);
                seekBarReverse.setProgress(0);

                /* Check timer and send off command to Arduino */

                countDownTimerRunning = false;
                updateWatchInterface();

                Toast.makeText(getApplicationContext(), "DC Motor Off, Job completed.", Toast.LENGTH_LONG).show();
                String cmdText = "<turn off>";
                assert cmdText != null;
                connectedThread.write(cmdText);
                beepSound.start();

                /* Controls disabled until on button is clicked */

                RPMDisplay.setText("0");
                DirectionText.setText("Off");
                seekBar.setEnabled(false);
                seekBarReverse.setEnabled(false);
                seekBar.setProgress(0);
                seekBarReverse.setProgress(0);
                buttonOn.setEnabled(true);
                buttonOff.setEnabled(false);
                inputTime.setEnabled(false);
                resetTimeButton.setEnabled(false);
                setTimeButton.setEnabled(false);
                startPauseTimeButton.setEnabled(false);
                ((ImageButton) findViewById(R.id.disconnectIcon)).setImageAlpha(0xFF );
                disconnectIcon.setEnabled(true);


            }
        }.start();

        countDownTimerRunning = true;
        updateWatchInterface();
    }

    private void cancelTimer() {


        if (countDownTimerRunning) {
            myCountDownTimer.cancel();
            updateWatchInterface();

        } else {
            updateWatchInterface();

        }
    }

    private void pauseTimer() {
        myCountDownTimer.cancel();
        countDownTimerRunning = false;
        updateWatchInterface();
    }

    private void resetTimer() {
        c_TimeLeftInMillis = c_StartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();

    }

    private void updateCountDownText() {

        String timeLeftFormatted;

        int hours = (int) (c_TimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((c_TimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (c_TimeLeftInMillis / 1000) % 60;


        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        countDownText.setText(timeLeftFormatted);
    }

    @SuppressLint("SetTextI18n")
    private void updateWatchInterface() {
        if (countDownTimerRunning) {
            inputTime.setVisibility(View.VISIBLE);
            setTimeButton.setVisibility(View.VISIBLE);
            resetTimeButton.setVisibility(View.VISIBLE);
            startPauseTimeButton.setText("Pause");
        } else {
            inputTime.setVisibility(View.VISIBLE);
            setTimeButton.setVisibility(View.VISIBLE);
            startPauseTimeButton.setText("Start");

            if (c_TimeLeftInMillis < 1000) {
                startPauseTimeButton.setVisibility(View.VISIBLE);
            } else {
                startPauseTimeButton.setVisibility(View.VISIBLE);
            }

            if (c_TimeLeftInMillis < c_StartTimeInMillis) {
                resetTimeButton.setVisibility(View.VISIBLE);
            } else {
                resetTimeButton.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", c_StartTimeInMillis);
        editor.putLong("millisLeft", c_TimeLeftInMillis);
        editor.putBoolean("timerRunning", countDownTimerRunning);
        editor.putLong("endTime", c_EndTime);

        editor.apply();

        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        c_StartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        c_TimeLeftInMillis = prefs.getLong("millisLeft", c_StartTimeInMillis);
        countDownTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if (countDownTimerRunning) {
            c_EndTime = prefs.getLong("endTime", 0);
            c_TimeLeftInMillis = c_EndTime - System.currentTimeMillis();

            if (c_TimeLeftInMillis < 0) {
                c_TimeLeftInMillis = 0;
                countDownTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
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

            CurrentAccel = (float) Math.sqrt(x * x + y * y + z * z);

            float delta = CurrentAccel - LastAccel;

            Acceleration = Acceleration * 0.9f + delta;

            if (Acceleration > 18) {

                final MediaPlayer beepSound = MediaPlayer.create(MainActivity.this,R.raw.beep);

                SeekBar seekBar = findViewById(R.id.seekBar);
                SeekBar seekBarReverse = findViewById(R.id.seekBarReverse);
                final TextView RPMDisplay = findViewById(R.id.RPMDisplayMain);
                final TextView DirectionText = findViewById(R.id.Dir);
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

                inputTime.setEnabled(false);
                resetTimeButton.setEnabled(false);
                setTimeButton.setEnabled(false);
                startPauseTimeButton.setEnabled(false);

                // Send command to Arduino board

                String cmdText = "<turn off>";
                assert cmdText != null; // Required to prevent crashes upon accidental drop as it attempts to send a BT command when BT not connected.
                connectedThread.write(cmdText);
                cancelTimer();
                resetTimer();

                beepSound.start();
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
                    rpmDataIntent.putExtra("RPM Data", rpmData);
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
        this.startActivity(new Intent(MainActivity.this,IntroActivity.class));

        return;
    }
}
