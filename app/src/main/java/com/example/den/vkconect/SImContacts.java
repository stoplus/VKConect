package com.example.den.vkconect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SImContacts extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout refreshLayout;
    private List<Contact> contactList;
    private AdapterContacts adapter;
    private RecyclerView recyclerView;
    private static final int REQUEST_PERMITIONS = 1100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_contacts);

        refreshLayout = findViewById(R.id.RefreshContacts);//получаем refreshLayout
        refreshLayout.setOnRefreshListener(this);//слушатель для обновления

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.idRecyclerViewContact);

        //Инициализация класса !!!-произойдет после компиляции-!!!
        SImContactsPermissionsDispatcher.requestPermissionsWithPermissionCheck(this);
    }//onCreate

    @NeedsPermission({Manifest.permission.READ_CONTACTS})
    void requestPermissions() {
        contactList = getSim();
        adapter = new AdapterContacts(this, contactList);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    // код для клика по элементу
                    @Override
                    public void onItemClick(View view, int position) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactList.get(position).getTel()));
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }//onItemClick

                    //длинное нажатие по элементу
                    @Override
                    public void onLongItemClick(View view, final int position) {
                    }//onLongItemClick
                })//RecyclerItemClickListener
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SImContactsPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }//onRequestPermissionsResult

    @OnPermissionDenied({Manifest.permission.READ_CONTACTS})
    void permissionsDenied() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMITIONS);
    }//permissionsDenied

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMITIONS) {
            SImContactsPermissionsDispatcher.requestPermissionsWithPermissionCheck(this);
        }
    }//onActivityResult

    @OnNeverAskAgain({Manifest.permission.READ_CONTACTS})
    void onNeverAskAgain() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Message")
                .setPositiveButton("Ok", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).create()
                .show();
    }

    @OnShowRationale({Manifest.permission.READ_CONTACTS})
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("Это надо вам!!!")
                .setPositiveButton("хорошо", (dialog, button) -> request.proceed())
                .setNegativeButton("низачто", (dialog, button) -> request.cancel())
                .show();
    }

    //========================================================================================================
    private List<Contact> getSim() {
        List<Contact> listContact = new ArrayList<>();
        String selection1 = ContactsContract.RawContacts.ACCOUNT_NAME + "='SIM'";
        String selection = ContactsContract.RawContacts.ACCOUNT_NAME + "='SIM1'";

        Cursor cursorSim = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, null, null);
        Cursor cursorSim1 = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection1, null, null);

        if (cursorSim != null && !cursorSim.moveToFirst()) {
            if (cursorSim1 != null && cursorSim1.moveToFirst()) {
                cursorSim = cursorSim1;
            }
        }
        cursorSim.moveToFirst();
        for (int i = 0; i < cursorSim.getCount(); i++) {
            Contact contact;
            String number = cursorSim.getString(cursorSim.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String name = cursorSim.getString(cursorSim.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            contact = new Contact(name, number);
            listContact.add(contact);
            cursorSim.moveToNext();
        }
        cursorSim.close();
        if (cursorSim1 != null) {
            cursorSim1.close();
        }
        refreshLayout.setRefreshing(false);//выключаем кружек-анимацию обновления
        return listContact;
    }

    //========================================================================================================
    //получаем список типов аккаунтов
    public void allSIMContact() {
        HashSet<String> accountTypes = new HashSet<>();
        String[] projection = new String[]{ContactsContract.RawContacts.ACCOUNT_NAME};
        Cursor cur = this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, null, null, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                accountTypes.add(cur.getString(0));
            }
        }
        for (String type : accountTypes) {
            Log.d("ACCOUNT_TYPE", type);
        }
        if (cur != null) {
            cur.close();
        }
    }

    //==============================================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem itemContacts = menu.findItem(R.id.mContacts); //определяем Item
        itemContacts.setVisible(false);//делаем невидимым
        MenuItem itemLogin = menu.findItem(R.id.mLogin); //определяем Item
        itemLogin.setVisible(false);//делаем невидимым

        return true;
    }//onLogout

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mLogout:
                AuthorizationUtils.logoutPref(this);
                AuthorizationUtils.onLogout(this);
                finish();
                return true;
            case R.id.mMain:
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.mStatus:
                finish();
                intent = new Intent(this, Status.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        contactList = getSim();
        adapter = new AdapterContacts(this, contactList);
        recyclerView.setAdapter(adapter);
    }
}
