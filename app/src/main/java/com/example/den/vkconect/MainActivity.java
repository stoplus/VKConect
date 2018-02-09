package com.example.den.vkconect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiCommunityFull;
import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layoutName;
    private TextView nameFile;
    private ImageView imageForSend;
    private List<String> listImage;
    private String img = "";
    private Account account = new Account();
    private Bitmap selectedImage;
    private EditText textMassege;

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
//        RecyclerView recyclerView = findViewById(R.id.idListView_Image);
        StaggeredGridLayoutManager mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(
                2, //number of grid columns
                GridLayoutManager.VERTICAL);

//        recyclerView.setLayoutManager(mStaggeredGridLayoutManager);

//        AdapterSelectImage adapter = new AdapterSelectImage(this, getFotoFromSD());
//        recyclerView.setAdapter(adapter);
//        recyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
//                    // код для клика по элементу
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        String img = listImage.get(position);
//                        int r = img.lastIndexOf('/');
//                        img = img.substring(r + 1);
//                        layoutName.setVisibility(View.VISIBLE);
//                        nameFile.setText(img);
//                    }//onItemClick
//
//                    //длинное нажатие по элементу
//                    @Override
//                    public void onLongItemClick(View view, final int position) {
//                    }//onLongItemClick
//                })//RecyclerItemClickListener
//        );
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
                Bitmap bitmap  = BitmapFactory.decodeStream(imageStream );
                //масштабируем под необходимый размер
                int maxHeight = 400;
                int maxWidth = 400;
                float scale = Math.min(((float)maxHeight / bitmap.getWidth()), ((float)maxWidth / bitmap.getHeight()));
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
    public void send(View view) {
        String text = textMassege.getText().toString();
        if (text.equals("") && selectedImage == null) {
            int f = 9;
            return;
        } else if (text.equals("") && selectedImage != null) {
            //oтправляем только картинку
            loadPhotoToMyWall(selectedImage, "");

        } else if (!text.equals("") && selectedImage == null) {
            //oтправляем только статус
            sendStatus(text);
        } else {
            //отправляем и статус и картинку
            sendStatus(text);
            loadPhotoToMyWall(selectedImage, "");
//            imageForSend.setImageBitmap(selectedImage);
        }

    }

    private void sendStatus(String newStatus) {
        VKRequest request = new VKRequest("status.set", VKParameters.from("text", newStatus));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                String status = "";
                try {
                    JSONObject jsonObject = response.json.getJSONObject("response");
                    status = jsonObject.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
            }

            @Override
            public void onError(VKError error) {
                // error
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
                // post was added
            }

            @Override
            public void onError(VKError error) {
                // error
            }
        });
    }//makePost

    @Override
    protected void onResume() {
        super.onResume();
        imageForSend = findViewById(R.id.imageForSend);
//        layoutName = findViewById(R.id.idLinerNameImage);
//        nameFile = findViewById(R.id.idNameImage);
        listImage = getFotoFromSD();
    }

    public List<String> getFotoFromSD() {
        File[] arrFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).listFiles();
        List<String> images = new ArrayList<>(); // массив имен файлов
        if (arrFile != null) {
            for (File lile : arrFile) {
                int r = lile.toString().lastIndexOf(".jpg");
                int e = lile.toString().lastIndexOf(".png");
                if (r != -1 || e != -1) images.add(lile.toString());
            }
        } else
            images.add("drawable://" + R.drawable.no_foto);
        return images;
    }//getFotoFromSD

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
            case R.id.mLogout: {
                AuthorizationUtils.logout(this);
                onLogout();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
    //+======================================================================
}//class MainActivity
