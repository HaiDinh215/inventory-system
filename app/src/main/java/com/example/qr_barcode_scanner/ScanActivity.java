package com.example.qr_barcode_scanner;

import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
        import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
        import android.os.Bundle;
import android.widget.Toast;

        import com.google.zxing.Result;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

        import static android.Manifest.permission.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private static final String DB_URL = "jdbc:mysql://192.168.2.27/inventory";
    private static final String USER = "user";
    private static final String PASS = "password";
    private static String scResult;

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

    //result handling when code is scanned
    @Override
    public void handleResult(Result result) {
        String scanResult = result.getText();
        ScanActivity.setScResult(scanResult);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                scannerView.resumeCameraPreview(ScanActivity.this);
            }
        });
//if code scanned contain a link then proceed to open that link
////        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
//            @Override
//            //if code scanned is a link and Visit is pressed then proceed to that link
//            public void onClick(DialogInterface dialog, int i) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));
//                startActivity(intent);
//            }
//        });
        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                btnConn();
            }
        });
        builder.setMessage(scanResult);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String getScResult() {
        return scResult;
    }

    public static void setScResult(String scResult) {
        ScanActivity.scResult = scResult;
    }

    private void btnConn() {
        Send objSend = new Send();
        objSend.execute("");
    }

    private static class Send extends AsyncTask<String, String ,String>{

        String msg = "";
        private static String newQuery;

        @Override
        protected String doInBackground(String... strings) {
            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                if(conn == null){
                    msg = "Connection goes wrong";
                }else{
                    Send.codeScannedCases();
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(newQuery);
                    msg = "Inserting Successful!!!";
                }
                conn.close();
            }
            catch (Exception e){
                msg = "Connection goes wrong";
                e.printStackTrace();
            }
            return msg;
        }

        public static void codeScannedCases(){
            String code = ScanActivity.getScResult();
            String name = "CND Cool Blue CoolBlue Hand Cleanser";
            String name1 = "Nature Bounty Hair Skin, Nails Gummies";
            String name2 = "Flexible Fabric Adhesive Bandages";
            String name3 = "Nature Bounty L-Lysine";

            switch (code){
                case "639370913391":
                    newQuery = "INSERT INTO test (Code, Name) VALUES('"+code+"', '"+name+"')";
                    break;
                case "029537657853":
                    newQuery = "INSERT INTO test (Code, Name) VALUES('"+code+"', '"+name1+"')";
                    break;
                case "062600731456":
                    newQuery = "INSERT INTO test (Code, Name) VALUES('"+code+"', '"+name2+"')";
                    break;
                case "029537060110":
                    newQuery = "INSERT INTO test (Code, Name) VALUES('"+code+"', '"+name3+"')";
                    break;
            }
        }
    }
}