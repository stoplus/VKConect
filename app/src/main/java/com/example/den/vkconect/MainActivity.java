package com.example.den.vkconect;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static com.example.den.vkconect.LoginActivity.scope;

public class MainActivity extends AppCompatActivity {
    private ImageView imageForSend;
    private Account account = new Account();
    private Bitmap selectedImage;
    private EditText textMassege;

    public static String STATUS_KEY = "status";
    public static String STATUS_AND_PHOTO_KEY = "statusAndPhoto";
    public static String PHOTO_KEY = "photo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //если пользователь не авторизован
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
        switch (reqCode) {
            case 10485://reqCode авторизации в ВК
                loginVK(reqCode, resultCode, data);
                break;
            case 111://reqCode системы при выборе картинок
                if (resultCode == RESULT_OK) {
                    selectImage(data);
                } else Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
                break;
        }
        super.onActivityResult(reqCode, resultCode, data);
    }//onActivityResult

    private void loginVK(int reqCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(reqCode, resultCode, data, new VKCallback<VKAccessToken>() {
            // Пользователь успешно авторизовался
            @Override
            public void onResult(VKAccessToken res) {
                account.access_token = res.accessToken;
                account.user_id = Long.parseLong(res.userId);
                account.save(MainActivity.this);//сохраняем access_token и user_id

                AuthorizationUtils.setAuthorized(MainActivity.this);//устанавливаем флаг true в преференсах
                Toast.makeText(MainActivity.this, "Авторизация прошла успешно.", Toast.LENGTH_LONG).show();
            }//onResult

            // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this, "Авторизация не пройдена!", Toast.LENGTH_LONG).show();
            }//onError
        })) {
            return;
        }//if
    }//loginVK

    private void selectImage(Intent data) {
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
        }//try-catch
    }//selectImage

    //======================================================================================
    @SuppressLint("ShowToast")
    public void send(View view) {
        String ip;
        String text = textMassege.getText().toString();
        if (text.equals("") && selectedImage == null) {
            Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Выберите фото и заполните статус", Snackbar.LENGTH_LONG).show();
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
                processingError(error, "Статус не опубликован!");
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
                processingError(error, "Фото не опубликовано!");
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
        });
    }//makePost

    private void processingError(VKError error, String mas){
        String massage = error.errorMessage;
        if (massage != null
                && massage.equals("java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.")) {
            Snackbar.make(findViewById(R.id.idCoordinatorLayout), "Не найден путь сертификации!", Snackbar.LENGTH_LONG).show();
        } else if (error.apiError.errorCode == 5) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Вы не авторизованы в ВК!")
                    .setMessage("Для отправки данных в ВК вам надо авторизоваться.")
                    .setPositiveButton("Авторизоваться", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            VKSdk.login(MainActivity.this, scope);
                        }
                    })
                    .setNegativeButton("Не отправлять", (dialog, button) -> dialog.dismiss())
                    .show();
        } else
            Snackbar.make(findViewById(R.id.idCoordinatorLayout), mas, Snackbar.LENGTH_LONG).show();
    }

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
    }//onLogout

    //+======================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.mMain); //определяем Item
        item.setVisible(false);//делаем невидимым
        return true;
    }//onLogout

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mLogout:
                AuthorizationUtils.logoutPref(this);
                onLogout();
                return true;
            case R.id.mStatus:
                finish();
                intent = new Intent(this, Status.class);
                startActivity(intent);
                return true;
            case R.id.mContacts:
                finish();
                intent = new Intent(this, SImContacts.class);
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
                        String ipAddress = inetAddress.getHostAddress();
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
