package com.example.logbook1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String m_Text = "";
    int counter = 0;
    TextView imageLink;
    ImageView imageView;
    Button previous,next,addLink,capturePhoto;
    List<String> list = new ArrayList<>();
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list.add("https://images.unsplash.com/photo-1663524789640-09950e318dac?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwzfHx8ZW58MHx8fHw%3D&auto=format&fit=crop&w=500&q=60");
        list.add("https://images.unsplash.com/photo-1663443525887-f2ef0cfc7684?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwxOHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=60");
        list.add("https://images.unsplash.com/photo-1663501981668-e3b19f8dada3?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwzOXx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=60");

        imageLink  = findViewById(R.id.textView);
        imageView  = findViewById(R.id.imageView);
        previous  = findViewById(R.id.button);
        next  = findViewById(R.id.button2);
        addLink  = findViewById(R.id.button3);
        capturePhoto  = findViewById(R.id.button4);

        config();
    }

    public void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void config() {
        ArrayList<String> listData = getArrayList("List");

        try {
            list.addAll(listData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Glide.with(this).load(list.get(counter)).into(imageView);

        imageLink.setText(list.get(counter));

        next.setOnClickListener(v -> {
            changePhoto(false);
        });
        previous.setOnClickListener(v -> {
            changePhoto(true);
        });
        addLink.setOnClickListener(v ->{
            openLinkDialog();
        });
        capturePhoto.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                takePhoto();
            }
        });
    }

    private void openLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Image Link");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            m_Text = input.getText().toString();
            if (m_Text == ""){
                Toast.makeText(this, "Image Link cannot be empty", Toast.LENGTH_SHORT).show();
            }else   if ( !URLUtil.isValidUrl(m_Text)){
                Toast.makeText(this, "Please provide valid URL", Toast.LENGTH_SHORT).show();
            }else{
                list.add(m_Text);
                List<String> listNew = new ArrayList<>();
                listNew.add(m_Text);
                saveArrayList((ArrayList<String>) listNew,"List");
            }

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void changePhoto(Boolean isPrevious) {
        if (isPrevious) {
            if (counter == 0) {
                Toast.makeText(this, "min limit reached", Toast.LENGTH_LONG).show();
            } else {
                counter = counter - 1;
                Glide.with(this).load(list.get(counter)).into(imageView);
                imageLink.setText(list.get(counter));
            }
        } else {
            if (counter == list.size() - 1) {
                Toast.makeText(this, "Max limit reached", Toast.LENGTH_LONG).show();
            } else {
                counter = counter + 1;
                Glide.with(this).load(list.get(counter)).into(imageView);
                imageLink.setText(list.get(counter));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void takePhoto() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else
        {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            String link = saveImage(thumbnail);
            list.add(link);
            Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
            List<String> listNew = new ArrayList<>();
            listNew.add(link);
            saveArrayList((ArrayList<String>) listNew,"List");
        }
    }

    public String saveImage(Bitmap myBitmap) {
    String IMAGE_DIRECTORY = "/YourDirectName";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
}

