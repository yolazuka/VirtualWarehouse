package com.example.usuario.virtualwarehouse.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Usuario on 22/7/17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    //Helper class that will intermediate between SQLite database and the user

    /**
     * This will be the name of our database file in SQLite
     */
    private static final String DATABASE_NAME = "myInventoryDATA.db";

    /**
     * This is the version number for the table. If we add new versions, this value should
     * increase.
     */
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //We instruct the program to create the data for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {

        // We declare the table in order to be readable for SQLite
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " ("
                + ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT + " TEXT NOT NULL DEFAULT 'no image', "
                + ProductContract.ProductEntry.COLUMN_NAME_PRODUCT + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT + " REAL NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT + " INTEGER DEFAULT 0);";

        // Execute the above declared
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    //This calling will be execute only when the database has been updated.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        onCreate(db);
    }

}
