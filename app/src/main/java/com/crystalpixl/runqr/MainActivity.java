package com.crystalpixl.runqr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ImageView imageIv;
    private MaterialButton scanBtn;
    private MaterialButton cameraBtn;
    private TextView resultTv;


    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    int qrcodenum = 1;

    private Uri imageUri = null;

    private BarcodeScanner barcodeScanner;

    private static final String TAG = "MAIN_TAG";

    BufferedWriter writer = null;
    File tempFile;
    ArrayList<String> hashes;
    ArrayList<String> values;
    String id;
    String finalvalue = "";

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.cameraBtn);
        imageIv = findViewById(R.id.imageIv);
        scanBtn = findViewById(R.id.scanBtn);
        ActionMenuItemView resetBtn = findViewById(R.id.resetBtn);
        scanBtn.setEnabled(false);
        values = new ArrayList<String>();
        resultTv = findViewById(R.id.resultTv);
        id = "";
        hashes();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        BarcodeScannerOptions barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        cameraBtn.setOnClickListener(view -> {

            if (checkCameraPermission()){
                pickImageCamera();
            }
            else {
                requestCameraPermission();
            }
        });

        scanBtn.setOnClickListener(view -> {
            if (imageUri == null){
                Toast.makeText(MainActivity.this, "Es wurde noch nichts gescannt.", Toast.LENGTH_SHORT).show();
                msg("Es wurde noch nichts gescannt.");
            } else {
                try {
                    play();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Fehler beim Verarbeiten des Bildes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    msg("Fehler beim Verarbeiten des Bildes: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.resetBtn) {
            // Restart the application by relaunching the main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void restart(Context context){
        ProcessPhoenix.triggerRebirth(context);
    }

    private void msg(String string){
        resultTv.setText(string);
    }

    private void hashes(){
        hashes = new ArrayList<String>();
        hashes.add("150b9f4aaa0daacdb71c9270777ac4c7");
        hashes.add("2e265a1bf3895aa12032734254631367");
        hashes.add("9ebead70f5d8797b1d0c10189aa304c8");
    }

    private void detectResultFromImage() {

        try {

            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            Log.d(TAG, "detectResultFromImage: "+ imageUri);

            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        try {
                            extractBarCodeQRCodeInfo(barcodes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "QR-Code konnte nicht gescannt werden, weil "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            msg("QR-Code konnte nicht gescannt werden, weil "+e.getMessage());
                    });
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "Fehlgeschlagen, weil "+e.getMessage(), Toast.LENGTH_SHORT).show();
            msg("Fehlgeschlagen, weil "+e.getMessage());
        }
    }

    public static String md5(String string)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(string.getBytes(StandardCharsets.US_ASCII),0,string.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            return String.format("%0" + (magnitude.length << 1) + "x", bi);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @SuppressLint("SetTextI18n")
    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes) throws IOException {

        for (Barcode barcode : barcodes) {

            String rawValue = barcode.getRawValue();
            Log.d(TAG, "extractBarCodeQRCodeInfo: rawValue: " + rawValue);

            Log.d(TAG, "extractBarCodeQRCodeInfo: "+id.isEmpty());
            if (id.isEmpty()){
                id = String.valueOf(rawValue.charAt(2));
            }
            Log.d(TAG, "extractBarCodeQRCodeInfo: "+id.equals(rawValue.charAt(2))+"/"+id+"/"+rawValue.charAt(2));

            if (id.equals(String.valueOf(rawValue.charAt(2)))){
                if (!values.contains(rawValue)) {
                    Log.d(TAG, String.valueOf(rawValue.charAt(0)));
                    values.add(rawValue);
                    scanBtn.setText("SPIELEN " + qrcodenum + "/" + rawValue.charAt(1));
                    Log.d(TAG, "-> " + values);
                    if (qrcodenum == Integer.parseInt(String.valueOf(rawValue.charAt(1)))) {
                        finalvalue = values.stream()
                                .sorted(Comparator.comparing(s -> s.charAt(0)))
                                .map(s -> s.substring(3))
                                .collect(Collectors.joining());
                        scanBtn.setEnabled(true);
                        cameraBtn.setEnabled(false);
                        Log.d(TAG, "extractBarCodeQRCodeInfo: Finalvalue: " + finalvalue);
                        Log.d(TAG, "extractBarCodeQRCodeInfo: Values: " + values);
                    }
                    qrcodenum++;
                    resultTv.setText("Erfolgreich gescannt.");
                }
                else {
                    Toast.makeText(MainActivity.this, "Sie haben diesen QR-Code schon gescannt.", Toast.LENGTH_SHORT).show();
                    msg("Sie haben diesen QR-Code schon gescannt.");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: Du hast diesen QR-Code schon gescannt.");
                }
            }
            else {
                Toast.makeText(MainActivity.this,"Dieser QR-Code gehört nicht zu der Reihe mit der sie angefangen habe.",Toast.LENGTH_SHORT).show();
                msg("Dieser QR-Code gehört nicht zu der Reihe mit der sie angefangen habe.");
            }

        }
    }


    private void play() throws IOException {
        tempFile = File.createTempFile("game", ".html");
        writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(finalvalue);
        writer.close();
        String url = tempFile.toURI().toURL().toString();

        Log.d(TAG, "play: URL to file: "+ url);
        Log.d(TAG, "play: md5hash: "+md5(finalvalue));
        if (hashes.contains(md5(finalvalue))){
            Intent i = new Intent(MainActivity.this, WebPlayer.class);
            i.putExtra("key",url);
            startActivity(i);
        }
        else {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent i = new Intent(MainActivity.this, WebPlayer.class);
                        i.putExtra("key",url);
                        startActivity(i);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(MainActivity.this, "Abgebrochen", Toast.LENGTH_SHORT).show();
                        msg("Abgebrochen");
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Der gescannte Code wurde nicht auf Viren geprüft, fortfahren?").setPositiveButton("Ja", dialogClickListener)
                    .setNegativeButton("Nein", dialogClickListener).show();
        }
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri: "+imageUri);
                        imageIv.setImageURI(imageUri);
                    }
                    else {

                        Toast.makeText(MainActivity.this, "Abgebrochen", Toast.LENGTH_SHORT).show();
                    }
                }
            }

    );

    private void pickImageCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == RESULT_OK){

                        Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                        imageIv.setImageURI(imageUri);
                        detectResultFromImage();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Abgebrochen", Toast.LENGTH_SHORT).show();
                        msg("Abgebrochen");
                    }
                }
            }
    );

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return resultCamera && resultStorage;
    }

    private void requestCameraPermission(){

        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0) {

                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (cameraAccepted && storageAccepted) {

                    pickImageCamera();
                } else {

                    Toast.makeText(this, "Es werden Kamera und Speicher Berechtigungen gebraucht", Toast.LENGTH_SHORT).show();
                    msg("Es werden Kamera und Speicher Berechtigungen gebraucht");
                }
            }
        }
    }
}