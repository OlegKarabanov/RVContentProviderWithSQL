package ru.synergi.rvcontentproviderwithsql;


import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.ViewGroup;


import androidx.recyclerview.widget.RecyclerView;



public abstract class CursorRecycleViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

    protected Context mContext; // сохраняем прямо здесь в классе

    private Cursor mCursor;

    private boolean mDataValid;

    private int mRowIdColumn;

    private DataSetObserver mDataSetObserver; // следим за изменением данных

    public CursorRecycleViewAdapter(Context context, Cursor cursor){  // делаем конструктор
        mContext = context; //нужен контекст
        mCursor = cursor;  // нужен курсор
        mDataValid = cursor != null;  // курсос не равен о
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id"): -1;//
        mDataSetObserver = new NotifyingDataSetObserver(this);
    }
    public Cursor getCursor() {return mCursor;}  //вспомогат метод, возвращаем курсос, доставать курсор как гетер
// организуем обязательные методы
    @Override
    public int getItemCount() { // считаем количество айтемов
        if(mDataValid && mCursor != null){
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {  // берет и считает отдельно айдишник нашего айтома
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)){
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public static final String TAG = CursorRecycleViewAdapter.class.getSimpleName();

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {return null;}

    @Override
    public void onBindViewHolder(VH viewHolder, int position ) { // ветвление
        if(!mDataValid){
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if(!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position" + position);
        }
        onBindViewHolder(viewHolder, mCursor); // возвращаем вьюхолдер
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null){
                old.close();
            }
        }

        public Cursor swapCursor(Cursor newCursor) {  // сменить один курсор на другой
            if (newCursor == mCursor) {
                return null;
            }

            final Cursor oldCursor = mCursor;
            if (oldCursor != null && mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }

            mCursor = newCursor;
            if (mCursor != null){
                if(mDataSetObserver != null) {
                    mCursor.registerDataSetObserver(mDataSetObserver);
                }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
        mRowIdColumn = -1;
        mDataValid =  false;
        notifyDataSetChanged();


            }

            return oldCursor;
        }

        public void setDataValid(boolean mDataValid) {this.mDataValid = mDataValid;}

    }


class NotifyingDataSetObserver extends DataSetObserver{ // создаем и наследуем класс
    private RecyclerView.Adapter adapter;

    public NotifyingDataSetObserver(RecyclerView.Adapter adapter) {this.adapter = adapter;} // метод для внутр использ и коннстр класс

    @Override
    public  void onChanged() {
        super.onChanged();
        ((CursorRecycleViewAdapter) adapter).setDataValid(true); // преобразовываем
        adapter.notifyDataSetChanged(); // ончендж будет распространять что данные измененились
    }

    @Override
    public void onInvalidated() {
        super.onInvalidated();
        ((CursorRecycleViewAdapter) adapter).setDataValid(false);
    }
}