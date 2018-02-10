package com.example.den.vkconect;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SImContacts extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout refreshLayout;
    private List<Contact> contactList;
    private AdapterContacts adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_contacts);

        refreshLayout = findViewById(R.id.RefreshContacts);//получаем refreshLayout
        refreshLayout.setOnRefreshListener(this);//слушатель для обновления

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.idRecyclerViewContact);
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

    //========================================================================================================
    private List<Contact> getSim() {
        List<Contact> listContact = new ArrayList<>();
        String selection1 = ContactsContract.RawContacts.ACCOUNT_NAME + "='SIM'";
        String selection = ContactsContract.RawContacts.ACCOUNT_NAME + "='SIM1'";

        Cursor cursorSim = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, selection, null, null);
        Cursor cursorSim1 = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, selection1, null, null);

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
    private void allSIMContact() {
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
