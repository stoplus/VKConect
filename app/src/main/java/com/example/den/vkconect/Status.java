package com.example.den.vkconect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.net.SocketException;


public class Status extends AppCompatActivity {
    private TextView ipPict;
    private TextView ipStat;
    private TextView ipPictAndStat;
    private TextView timePhoto;
    private TextView timeStat;
    private TextView timePhotoAndStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ipPict = findViewById(R.id.IPPict);
        ipStat = findViewById(R.id.IPStut);
        ipPictAndStat = findViewById(R.id.IPPictAndStut);
        timePhoto = findViewById(R.id.timePict);
        timeStat = findViewById(R.id.timeStat);
        timePhotoAndStat = findViewById(R.id.timePictAndStat);

        //Востанавливаем все значения для таблици
        Account account = new Account();
        String photo = account.restorePhoto(this);
        String stat = account.restoreStat(this);
        String photoAndStat = account.restorePhotoAndStat(this);
        String photoTime = account.restorePhotoTime(this);
        String StatTime = account.restoreStatTime(this);
        String PhotoAndStatTime = account.restorePhotoAndStatTime(this);

        ipPict.setText(photo == null ? "—" : photo);
        ipStat.setText(stat == null ? "—" : stat);
        ipPictAndStat.setText(photoAndStat == null ? "—" : photoAndStat);
        timePhoto.setText(photoTime == null ? "—" : photoTime);
        timeStat.setText(StatTime == null ? "—" : StatTime);
        timePhotoAndStat.setText(PhotoAndStatTime == null ? "—" : PhotoAndStatTime);
    }

    //=========================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.mStatus); //определяем Item
        item.setVisible(false);//делаем невидимым
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
            case R.id.mContacts:
                finish();
                intent = new Intent(this, SImContacts.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
