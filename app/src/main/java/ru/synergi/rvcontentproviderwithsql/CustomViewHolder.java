package ru.synergi.rvcontentproviderwithsql;

import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomViewHolder extends RecyclerView.ViewHolder {
    public TextView textView1; // по умолчанию будет текствью

    public CustomViewHolder(@NonNull View itemView){
        super(itemView); // импортируем текствью
        textView1 = (TextView) itemView.findViewById(android.R.id.text1); // находим текствью1, поле стандартной разметки

    }

    public void setData(Cursor c) {
        textView1.setText(c.getString(c.getColumnIndex("text")));
    }
//дергаем метод сетдата который исп в дальнейшем, берем ииндекс и плюсуем его к названию


}
