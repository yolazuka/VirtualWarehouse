package com.example.usuario.virtualwarehouse.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Usuario on 22/7/17.
 */

public class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider. The main reference for the contract and
     * the rest of the classes.
     */
    public static final String CONTENT_AUTHORITY = "com.example.usuario.virtualwarehouse";

    /**
     *  CONTENT_AUTHORITY is used in order to create the URI,S that will connect with the rest of the
     * apps into, our out of our device.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCT = "myinventory";

    // It,s private in order to avoid the creation of other similar classes. This class
    //has to be unique.
    private ProductContract() {
    }

    public static class ProductEntry implements BaseColumns {

        /**
         * We link through append our Content Authority name( Content_Authority ) with our Path Product
         * in order to create a final CONTENT URI
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        /**
         * Type MIME from the CONTENT URI in order to get a whole list of products
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * Type MIME from our CONTENT_URI in order to get a single product. ( an specific one )
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        public static final String TABLE_NAME = "myinventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_IMAGE_PRODUCT = "Product_Image";
        public static final String COLUMN_NAME_PRODUCT = "Product_Name";
        public static final String COLUMN_PRICE_PRODUCT = "Product_Price";
        public static final String COLUMN_QUANTITY_PRODUCT = "Product_Quantity";

        // Static values for the image categories

        public static final String IMAGE_TYPE_NONE = "image_type_none";
        public static final String IMAGE_STATIONERY = "stationery_images";
        public static final String IMAGE_BOOKS = "books_images";
        public static final String IMAGE_PRESENTS = "presents_images";
    }
}
