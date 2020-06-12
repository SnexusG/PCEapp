package com.example.pce_app;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private Switch btSwitch;
    private EditText setText;
    private Button setTextBtn;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    boolean hc_05_exists = false;
    Set<BluetoothDevice> pairedDevices = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setText = findViewById(R.id.lcd_text);

        btSwitch = findViewById(R.id.BTswitch);

        setTextBtn = findViewById(R.id.setTextBtn);

        if (!bluetoothAdapter.isEnabled()) {
            //bluetooth is off
            btSwitch.setChecked(false);
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            // btSwitch.setChecked(true);
        } else {
            btSwitch.setChecked(true);
        }

        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSwitch.isChecked()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_ENABLE_BT);
                } else {
                    bluetoothAdapter.disable();
                }
            }
        });

        if (btSwitch.isChecked()) {

            System.out.println(bluetoothAdapter.getBondedDevices());
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            //checking if hc-05 is paired
            if(pairedDevices.size() > 0){

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    System.out.println(deviceName);
                    if(deviceName.equals("HC-05")){
                        hc_05_exists = true;
                        System.out.println("HC_05 max address" +device.getAddress());
                        break;
                    }
                }
            }


            if(hc_05_exists){
                System.out.println("HC_05 exists in bonded devices");
                BluetoothDevice hc_05 = bluetoothAdapter.getRemoteDevice("FC:A8:9A:00:1B:14");
                BluetoothSocket btSocket = null;
                btSocket = null;
                int counter = 0;

                //trying to connect three times
                do {
                    try {
                        btSocket = hc_05.createRfcommSocketToServiceRecord(mUUID);
                        System.out.println(btSocket);
                        btSocket.connect();
                        System.out.println(btSocket.isConnected());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    counter++;
                } while (!btSocket.isConnected() && counter < 3);

                if(btSocket.isConnected()){
                    try {
                        final OutputStream outputStream = btSocket.getOutputStream();
                        setTextBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setLCDText(outputStream);
                            }
                        });
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }

            }else{
                Toast.makeText(this, "Please pair hc-05 to the android phone and restart the application", Toast.LENGTH_SHORT).show();
            }


        }else{
            Toast.makeText(this, "Please turn on Bluetooth for operating the LCD", Toast.LENGTH_SHORT).show();
        }

    }

    public void setLCDText(OutputStream outputStream){

                try {
                    String lcdText = setText.getText().toString();
                    Toast.makeText(this, lcdText, Toast.LENGTH_SHORT).show();
                    outputStream.write(lcdText.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful

            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                btSwitch.setChecked(true);
                //  findDevices();
                // Do something with the contact here (bigger example below)
            }else{
                Toast.makeText(this, "Bluetooth couldn't be turned on!", Toast.LENGTH_SHORT).show();
                btSwitch.setChecked(false);
            }
        }
    }
}
