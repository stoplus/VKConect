package com.example.den.vkconect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageView imageForSend;
    private Account account = new Account();
    private Bitmap selectedImage;
    private EditText textMassege;

    public static String STATUS_KEY = "status";
    public static String STATUS_AND_PHOTO_KEY = "statusAndPhoto";
    public static String PHOTO_KEY = "photo";
    private boolean foto = false;
    private boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //если пользователь авторизован
        if (!AuthorizationUtils.isAuthorized(this)) {
            onLogout();
            return;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textMassege = findViewById(R.id.editText);
        StaggeredGridLayoutManager mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(
                2, //number of grid columns
                GridLayoutManager.VERTICAL);
    }//onCreate


    //======================================================================================
    public void selectImage(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 111);
    }//selectImage

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                //масштабируем под необходимый размер
                int maxHeight = 400;
                int maxWidth = 400;
                float scale = Math.min(((float) maxHeight / bitmap.getWidth()), ((float) maxWidth / bitmap.getHeight()));
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                selectedImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                imageForSend.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }//onActivityResult

    //======================================================================================
    @SuppressLint("ShowToast")
    public void send(View view) {
        String ip = "";
        String text = textMassege.getText().toString();
        if (text.equals("") && selectedImage == null) {
            Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Выберите фото и заполните статус", Snackbar.LENGTH_LONG).show();
            return;
        } else if (text.equals("") && selectedImage != null) {
            //oтправляем только картинку
            loadPhotoToMyWall(selectedImage, "");
            ip = getLocalIpAddress();
            account.savePhoto(this, ip);
        } else if (!text.equals("") && selectedImage == null) {
            //oтправляем только статус
            sendStatus(text);
            ip = getLocalIpAddress();
            account.saveStatus(this, ip);
        } else {
            //отправляем и статус и картинку
            sendStatus(text);
            loadPhotoToMyWall(selectedImage, "");
            ip = getLocalIpAddress();
            account.saveStatusAndPhoto(this, ip);
            if (status && foto){
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Статус и фото опубликованы!", Snackbar.LENGTH_LONG).show();
            }else if (!status && foto){
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Только фото опубликовано!", Snackbar.LENGTH_LONG).show();
            }else if (status && !foto){
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Только статус опубликован!", Snackbar.LENGTH_LONG).show();
            }else Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Не опубликовано!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void sendStatus(String newStatus) {
        VKRequest request = new VKRequest("status.set", VKParameters.from("text", newStatus));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Новый статус опубликован", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Статус не опубликован!", Snackbar.LENGTH_LONG).show();
            }
        });
    }//sendStatus


    private void loadPhotoToMyWall(final Bitmap photo, final String message) {
        VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.jpgImage(0.9f)), getMyId(), 0);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                makePost(new VKAttachments(photoModel), message, getMyId());
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Фото опубликовано", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Фото не опубликовано!", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    int getMyId() {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        return vkAccessToken != null ? Integer.parseInt(vkAccessToken.userId) : 0;
    }//loadPhotoToMyWall

    private void makePost(VKAttachments att, String msg, final int ownerId) {
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.OWNER_ID, String.valueOf(ownerId));
        parameters.put(VKApiConst.ATTACHMENTS, att);
        parameters.put(VKApiConst.MESSAGE, msg);
        VKRequest post = VKApi.wall().post(parameters);
        post.setModelClass(VKWallPostResult.class);
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Фото опубликовано", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                foto = false;
            }
        });
    }//makePost

    @Override
    protected void onResume() {
        super.onResume();
        imageForSend = findViewById(R.id.imageForSend);
    }

    //	If user is not authorized we finish the main activity
    private void onLogout() {
        account.access_token = null;
        account.user_id = 0;
        account.save(MainActivity.this);
        Intent login = new Intent(this, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(login);
        finish();
    }

    //+======================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }//onLogout

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mLogout:
                AuthorizationUtils.logoutPref(this);
                onLogout();
                return true;
            case R.id.mStatus:
                finish();
                Intent intent = new Intent(this, Status.class);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //+======================================================================
    public String getLocalIpAddress() {

        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddress = inetAddress.getHostAddress().toString();
                        Log.e("IP address", "" + ipAddress);
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }//getLocalIpAddress
}//class MainActivity
