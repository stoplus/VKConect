package com.example.den.vkconect;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Наш on 07.07.2017.
 */

public class AdapterSelectImage extends RecyclerView.Adapter<AdapterSelectImage.ViewHolder> {
    private LayoutInflater inflater;    // для загрузки разметки элемента
    private List<String> imageList;    // коллекция выводимых данных

    public AdapterSelectImage(Context context, List<String> imageList) {
        this.inflater = LayoutInflater.from(context);
        this.imageList = new ArrayList<>(imageList);
    }//AdapterForAdmin

    @Override
    public int getItemCount() {
        return imageList.size();
    }//getItemCount

    @Override
    public long getItemId(int position) {
        return position;
    }//getItemId

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_list_view, parent, false);
        return new ViewHolder(view);
    } // onCreateViewHolder

    //внутрений класс ViewHolder для хранения элементов разметки
    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        // в конструкторе получаем ссылки на элементы по id
        private ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.idIVSelectImage);

        }//ViewHolder
    }//class ViewHolder

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // связать отображаемые элементы и значения полей
        holder.image.setImageDrawable(Drawable.createFromPath(imageList.get(position)));
    }//onBindViewHolder
}