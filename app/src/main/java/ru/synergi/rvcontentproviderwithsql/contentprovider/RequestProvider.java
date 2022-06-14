package ru.synergi.rvcontentproviderwithsql.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.synergi.rvcontentproviderwithsql.tablemoc.BuildConfig;
import ru.synergi.rvcontentproviderwithsql.tablemoc.CustomSqliteOpenHelper;
import ru.synergi.rvcontentproviderwithsql.tablemoc.TableItems;

public class RequestProvider extends ContentProvider {

    private static final String TAG = "RequestProvider";
    private SQLiteOpenHelper mSqliteOpenHelper;
    private static final UriMatcher sUriMatcher;

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".db";

    public static final int TABLE_ITEMS = 0;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TableItems.NAME + "/offset/" + "#", TABLE_ITEMS);
    }

    private static Uri urlForItems(int limit) {
        return Uri.parse("content://" + AUTHORITY + "/" + TableItems.NAME + "/offset/" + limit);
    }


    @Override
    public boolean onCreate() {
        mSqliteOpenHelper = new CustomSqliteOpenHelper(getContext()); // гарантируем что база создана
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mSqliteOpenHelper.getReadableDatabase(); // получаем базу данных с правами чтения
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder(); // команда для выбора чго-то из базы данных
        //создаем логику для поведения программы в случае запроса у провайдера по к-либо данным
        Cursor c = null;
        String offset = null;

        switch (sUriMatcher.match(uri)) {     // еслм юрайка матчится с текстом по которому мы можем работать, то
            case TABLE_ITEMS:  //выбрали из все юрайки текст где мы указываем тайблайтем
                sqb.setTables(TableItems.NAME); //создаем запрос
                offset = uri.getLastPathSegment(); // получаем через юрай наше значение
                break;
            default:
                break;
        }
        int intOffset = Integer.parseInt(offset); //хотим получить полноценное число из нашей строки
        String limitArg = intOffset + ", " + 30;
        Log.d(TAG, "query :" + limitArg);
        c = sqb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limitArg);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return BuildConfig.APPLICATION_ID + ".item";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table = "";
        switch (sUriMatcher.match(uri)) {  //если будет совпадать с юрай
            case TABLE_ITEMS: {  // если выделенная часть из матчера равна тэйблматчес
                table = TableItems.NAME; // если таблица та, то мы берем ее
                break;
            }
        }
        //надо взять записываемую баз данных , и все конфликты игнорим
        long result = mSqliteOpenHelper.getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) { // если наш резулт будет равен минус одному то мы проборосим новый экуэль эспеп, мы не смогли заинсертить
            throw new SQLException("insert with conflict");
           } // иначе возвращаем юрай   на контент
    Uri retUri = ContentUris.withAppendedId(uri, result); // мы ставим наш юрай и резалт просто приклеиваем юрай и резалт и отправляем на выход
        return retUri; // возращаем юрай
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionAgrs) {
        return -1; // если стоит ноль, то он как бы не реализованный
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return -1;
    }
}
