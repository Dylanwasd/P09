package sg.edu.rp.c346.p09;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    LocationRequest mLocationRequest;
    FusedLocationProviderClient client;
    LocationCallback mLocationCallback;
    TextView tvLat;
    TextView tvLong;
    Button start;
    Button stop;
    String folderLocation;
    Button check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLat = findViewById(R.id.tvLat);
        tvLong = findViewById(R.id.tvLong);
        client = LocationServices.getFusedLocationProviderClient(this);
        stop = findViewById(R.id.btnStop);
        start = findViewById(R.id.btnStart);
        check = findViewById(R.id.btnCheck);

        int permissionCheck_Storage = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
        if(permissionCheck_Storage != PermissionChecker.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
            finish();
        }
        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Location";

        File folder = new File(folderLocation);
        if(folder.exists() == false){
            boolean result = folder.mkdir();
            if(result == true){
                Log.d("File Read/Write", "Folder created");
            }else{
                Log.d("File Read/Write", "Folder creation failed");
            }
        }

        if (checkPermission() == true ){
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        Location data = locationResult.getLastLocation();
                        double lat = data.getLatitude();
                        double lng = data.getLongitude();
                        tvLat.setText(String.valueOf(lat));
                        tvLong.setText(String.valueOf(lng));
                        File targetFile = new File(folderLocation, "data.txt");

                        try {
                            FileWriter writer = new FileWriter(targetFile, true);
                            writer.write(lat +", " + lng +"\n");
                            writer.flush();
                            writer.close();

                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to Write!",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                };
            };
        }

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File targetFile = new File(folderLocation, "data.txt");
                if(targetFile.exists() == true ){
                    String data = "";
                    try{
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while(line != null){
                            data += line + "\n";
                            line = br.readLine();
                        }
                        br.close();
                        reader.close();

                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.removeLocationUpdates(mLocationCallback);
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
                Toast.makeText(MainActivity.this, "Service Stopped", Toast.LENGTH_LONG).show();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
                Intent i = new Intent(MainActivity.this, MyService.class);
                startService(i);
                Toast.makeText(MainActivity.this, "Service Started", Toast.LENGTH_LONG).show();
            }
        });

    }
    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION
        );
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION
        );

        if(permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            return true;
        }else{
            String msg = "Permission not granted to retrieve location info";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},0);
            return false;
        }
    }
}
