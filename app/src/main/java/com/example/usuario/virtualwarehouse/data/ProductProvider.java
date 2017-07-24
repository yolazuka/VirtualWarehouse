package com.example.usuario.virtualwarehouse.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Usuario on 22/7/17.
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductContract.ProductEntry.class.getSimpleName();

    //URI matcher we add a code for the full table, in order to get an easier understanding at reading the code.
    private static final int FULL_TABLE = 100;

    //URI matcher we add a code for a single row ( item or product ) in order to get an easier understanding at reading the code.
    private static final int ID_TABLE = 101;

    /**
     * UriMatcher object is used to find and match a content URI with a suitable code.
     * The input passed into the constructor returns the code for the root URI.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // We initialize the matcher for the first time in a static mode
    static {
        // The uri matcher object adds a URI, for all of the content URI patterns that the provider
        // has to spot. All paths added to the UriMatcher have a corresponding code in return.

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT,
                FULL_TABLE);

        // The content URI of the form "content://com.example.joni.inventoryapp/inventoryapp/#" will
        // map to the integer code D_TABLE. Once this has been made, This URI will provide access to ONE
        // single row of the products table.
        // The "#" wildcard can be substituted for an integer.

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT
                + "/#", ID_TABLE);
    }

    //Helper object for SQLiteDataBase

    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        // We create an object from DBHelper
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // We call the method getReadableDatabase throught the helper in order to obtein
        // a readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // We initialize this cursor variable , because this is the "bundle" that will gather
        //all the information from the table to return it
        Cursor cursor;

        // We explain here the path to follow in case the URI is for a single product or for the
        //whole list of products
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FULL_TABLE:
                // We say here, what the cursor is gonna get from the table
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ID_TABLE:
                // For the ID_TABLA code, extract out the ID from the URI.
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This declaration will create a  query for the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unable to reach the query for the URI: " + uri);
        }

        // Method that let us know if the table needs to be updated due to recent changes

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return all the data gathered into the Cursor cursor.
        return cursor;
    }

    @Override

    //Conditional statement to establish if the ContentValues match the object sUriMatcher
    // if they dont match, throw an exception
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FULL_TABLE:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Unable to insert the values for the URI: " + uri);
        }
    }

    //In this following block of code we are going to insert a new product, getting as
    //a result the values and the URI id of that new product.

    private Uri insertProduct(Uri uri, ContentValues values) {

        // Conditional statement to check if the name is null. If is not null, get the result,
        // if its null, return an error message
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT);
        if (name == null) {
            throw new IllegalArgumentException("Sorry, the product requires a name");
        }

        // Conditional statement to check if the price is null. If is not null, get the result,
        // if its null, return an error message
        Float price = values.getAsFloat(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Sorry, the field requires a price");
        }

        // We call the method getWritableDatabase throught the helper in order to obtein
        // a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Now insert the new product into a new row into the table.
        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        // If the insertion fails, then return null
        if (id == -1) {
            return null;
        }

        // Get notification of the made changes.
        getContext().getContentResolver().notifyChange(uri, null);

        // Now it returns a new URI + ID from the new created product.
        return ContentUris.withAppendedId(uri, id);
    }

    @Override

    //In this following block of text, we are going to give the instructions
    //for an existing product updating case, through the method update and
    //a conditional statement that will cover the exceptions.

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FULL_TABLE:
                return updateProduct(uri, values, selection, selectionArgs);
            case ID_TABLE:

                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unable to update the changes into the URI: " + uri);
        }
    }

    /**
     * Actualice los productos de la base de datos con los valores de contenido especificados.
     * Aplique los cambios a las filas especificadas en los argumentos de selección y selección
     * (que podrían ser 0 o 1 o más productos). Devuelve el número de filas que se actualizaron
     * correctamente.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Si la clave {@link ProductEntry # COLUMN_NAME_PRODUCT} está presente, compruebe que el
        // valor del nombre no es nulo.
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT)) {
            String name = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT);
            if (name == null) {
                return 0;
            }
        }

        //Si la clave {@link ProductEntry.COLUMN_PRICE_PRODUCT} está presente, compruebe que el
        // valor del precio no sea nulo o igual a 0.
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT)) {
            Float precio = values.getAsFloat(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT);
            if (precio == null || precio == 0) {
                return 0;
            }
        }

        // Conditional that says: if there is not updated data, dont update anything
        if (values.size() == 0) {
            return 0;
        }

        // If there is, get updated info in the data base and return a writable one.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // In the case of changes, update and ge the numbers of rows affected by the change.
        int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // Notify of the change if one or more rows have changed.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // and return the number of updated rows.
        return rowsUpdated;
    }

    @Override
    //In this following block of text, we are going to give the instructions
    //for deleting an existing product, through the method delete and
    //conditional statements that will cover the exceptions.

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get a writable database through the helper
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // It tell us how many rows were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FULL_TABLE:
                // Delete all the tables that match the below parameters
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ID_TABLE:
                // Delete the row that match the below indicated ID
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unable to read data from the URI: " + uri);
        }

        // If one or more rows were deleted, please notify the change, through the method notifyChange
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // and now , return the number of deleted rows.
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FULL_TABLE:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case ID_TABLE:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("URI Unknown " + uri + " does not match with : " + match);
        }
    }
}
