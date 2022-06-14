package ru.synergi.rvcontentproviderwithsql;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class CustomCursorRecyclerViewAdapter extends CursorRecycleViewAdapter { // наслед от кастомрекиьюадапт



    public CustomCursorRecyclerViewAdapter(Context context, Cursor cursor) { // передаем контекст и курсор
        super(context, cursor); // передаем в родителя
    }

    @Override
    public long getItemId(int position){
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // есть элемент иью и в него надо заинфлуейтить нашу разметку, которую создаем
        View v = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new CustomViewHolder(v); // возвращаем КВХ и передаем в него v
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) { // создаем новый вьюхолдер
        CustomViewHolder holder = (CustomViewHolder) viewHolder;
        cursor.moveToPosition(cursor.getPosition()); // сдвигаем курсор на нужную позицию
        holder.setData(cursor); // даем данные на вход (обязатльно)

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
