package com.example.qr_barcode_scanner;

import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

        import android.Manifest;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Bundle;
        import android.widget.Toast;

        import com.google.zxing.Result;

        import me.dm7.barcodescanner.zxing.ZXingScannerView;

        import static android.Manifest.permission.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        //check if device version is atleast 23 or later
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                Toast.makeText(ScanActivity.this, "Permission is granted!", Toast.LENGTH_LONG).show();
            }else{
                requestPermission();
            }
        }
    }

    //check if permission is granted
    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(ScanActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    //request for camera permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionResult(int requestCode, String permission[], int grantResults[]){
        switch (requestCode){
            case REQUEST_CAMERA:
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        Toast.makeText(ScanActivity.this,"Permission Granted", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(ScanActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                displayAlertMessage("You need to allow access for both permissions", new DialogInterface.OnClickListener(){

                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                    }
                                });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                if(scannerView == null){
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }else{
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(ScanActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        String scanResult = result.getText();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                scannerView.resumeCameraPreview(ScanActivity.this);
            }
        });
        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            //if code scanned is a link and Visit is pressed then proceed to that link
            public void onClick(DialogInterface dialog, int i) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));
                startActivity(intent);
            }
        });
        builder.setMessage(scanResult);
        AlertDialog alert = builder.create();
        alert.show();
    }
}