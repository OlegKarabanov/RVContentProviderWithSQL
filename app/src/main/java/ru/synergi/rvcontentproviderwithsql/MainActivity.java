package ru.synergi.rvcontentproviderwithsql;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import ru.synergi.rvcontentproviderwithsql.contentprovider.RequestProvider;
import ru.synergi.rvcontentproviderwithsql.tablemoc.TableItems;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, androidx.loader.app.LoaderManager.LoaderCallbacks<D> {
// мы хотим использовать лоадеры, быстро получ инф из курсора
    public final int offset = 30;
    private int page = 0;

    private RecyclerView mRecyclerView;
    private boolean loadingMore = false;
    private Toast shortToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager mlayoutManager = new LinearLayoutManager(this); // специф для рекуклер иью
        CustomCursorRecyclerViewAdapter mAdapter = new CustomCursorRecyclerViewAdapter(this, null);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView); // находим рекуклер вью
        mRecyclerView.setLayoutManager(mlayoutManager); // ставим лейаут менеджер
        mRecyclerView.setAdapter(mAdapter); // ставим адаптер

        int itemsCountLocal = getItemCountLocal(); // вызываем метод
        if (itemsCountLocal == 0) { // если 0
            fillTestElements(); // все заполняем тест элемент
        }

        shortToast = Toast.makeText(this, "", Toast.LENGTH_SHORT); // у нас есть пустой текст

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {  // на рекуслер иью вешаем скроллтистенер
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager =(LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int maxPositions = layoutManager.getItemCount();
//когда мы долистали до нужной опзиции нам надо загрузить еще элементов
                if(lastVisibleItemPosition == maxPositions -1) {
                    if(loadingMore)
                        return;

                    loadingMore = true;
                    page++; // пандинация, когда нам нужно загрузить  еще контент сверху
                    getSupportLoaderManager().restartLoader(0, null, MainActivity.this); // загружаем дальше
                }
            }
        });

        getSupportLoaderManager().restartLoader(0,null, this); // рестартим лоудер, все колбэки ссылаем в лоадерменеджер

    }

    private void fillTestElements() {
        int size = 1000;
        ContentValues[] cvArray = new ContentValues[size]; // создаем массив
        for (int i = 0; i < cvArray.length; i++) { // засовываем в один большой массив
            ContentValues cv = new ContentValues();
            cv.put(TableItems.TEXT, ("text " +1));
            cvArray[i] = cv;
        }

        getContentResolver().bulkInsert(RequestProvider.urlForItems(0), cvArray); // вставляем все значения в базу банных
    }  // зашли в базу данных, увидели что каунт равен о, то тогда мы контентрезолвером сами выставляем все значения от 1 до 1000, с текстами текст+1 и т.д.

    private int getItemCountLocal(){
        int itemsCount = 0;

        Cursor query = getContentResolver().query(RequestProvider.urlForItems(0), null, null, null, null);
        if (query != null) {
            itemsCount = query.getCount();  // проверяем сколько есть значений
            query.close();
        }
        return itemsCount;

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){  // сработает при загрузки данных в лаудер, при работе с лаудерколлбэкс
        switch (id) {
            case 0:
                return new CursorLoader(this, RequestProvider.urlForItems(offset * page), null,null,null,null);
            default:
                throw  new IllegalArgumentException("no id handled!");

        }
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) { //вернут лаудер и данные которые он смог добыть
        switch (loader.getId()) {
            case 0:
                Log.d(TAG, "onLoadFinished: loading MORE");
                shortToast.setText("loading MORE " + page);
                shortToast.show();

                Cursor cursor = ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).getCursor(); // достаем данные из курсора

                MatrixCursor mx = new MatrixCursor(TableItems.Columns);  // доставем из матрикс курсор
                fillMx(cursor, mx);

                fillMx(data, mx); // добавляем дополнитеьный результат

                ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).swapCursor(mx); // меняем адаптер на курсоре чтобы отбразить новые данные
                handlerToWait.postDelayed(new Runnable() {  // есть задача с отложенным выполнением
                    @Override
                    public void run() {loadingMore = false;} // загрузить еще
                }, 2000); // через 2 секунды
                break;
            default:
                throw new IllegalArgumentException("no loader id handled!");
        }

    }

    private Handler handlerToWait = new Handler();

    private void fillMx(Cursor data, MatrixCursor mx) {
        if (data == null)
            return;

        data.moveToPosition(-1); //
        while (data.moveToNext()) { // пока есть что читать - читаем
            mx.addRow(new Object[]{ // и заполняем их в Роу
                    data.getString(data.getColumnIndex(TableItems._ID)),
                    data.getString(data.getColumnIndex(TableItems.TEXT))
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static final String TAG = "MainActivity";

}
