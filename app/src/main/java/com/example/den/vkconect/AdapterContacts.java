package com.example.den.vkconect;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by den on 10.02.2018.
 */

public class AdapterContacts extends RecyclerView.Adapter<AdapterContacts.ViewHolder> {
    private LayoutInflater inflater;    // для загрузки разметки элемента
    private List<Contact> contactList;    // коллекция выводимых данных
    private Resources res;

    public AdapterContacts(Context context, List<Contact> contactList) {
        this.inflater = LayoutInflater.from(context);
        this.contactList = new ArrayList<>(contactList);
    }//AdapterForAdmin

    @Override
    public int getItemCount() {
        return contactList.size();
    }//getItemCount

    @Override
    public long getItemId(int position) {
        return position;
    }//getItemId

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    } // onCreateViewHolder

    //внутрений класс ViewHolder для хранения элементов разметки
    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameContact;
        final TextView telContact;

        // в конструкторе получаем ссылки на элементы по id
        private ViewHolder(View view) {
            super(view);
            nameContact = view.findViewById(R.id.idCont);
            telContact = view.findViewById(R.id.idTel);
            res = view.getResources();//доступ к ресерсам
        }//ViewHolder
    }//class ViewHolder

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // связать отображаемые элементы и значения полей
        if (position % 2!=0){
            holder.nameContact.setBackgroundColor( res.getColor(R.color.vk_white));
            holder.telContact.setBackgroundColor( res.getColor(R.color.vk_white));
        }
        holder.nameContact.setText(contactList.get(position).getName());
        holder.telContact.setText(contactList.get(position).getTel());
    }//onBindViewHolder
}

