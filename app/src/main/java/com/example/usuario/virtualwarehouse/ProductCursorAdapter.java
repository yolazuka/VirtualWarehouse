package com.example.usuario.virtualwarehouse;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.virtualwarehouse.data.ProductContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Usuario on 22/7/17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private final MainActivity activity;

    public ProductCursorAdapter(MainActivity context, Cursor c) {
        super(context, c, 0);
        this.activity = context;
    }

    @Override

    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate a list of views, linking it with the view  list_items.xml

        return LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find the views in xml and link it with our java code.

        TextView textViewName = (TextView) view.findViewById(R.id.product_name_listItems);
        TextView textViewPrice = (TextView) view.findViewById(R.id.price_listItems);
        TextView textViewQuantity = (TextView) view.findViewById(R.id.quantity_listItems);

        ImageView imageViewImage = (ImageView) view.findViewById(R.id.product_image);

        ImageView shoppingButton = (ImageView) view.findViewById(R.id.shopping_button);

        // Find the columns of the database, and link it with the Sqlite variables previously declared

        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT);
        final int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT);
        int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT);

        // Now we are going to get the strings from these attributes through the cursor

        int id = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        Uri productImage = Uri.parse(cursor.getString(imageColumnIndex));
        final String productName = cursor.getString(nameColumnIndex);
        final double productPrice = priceColumnIndex;
        String displayPrice = "Price: " + cursor.getString(priceColumnIndex) + " €";
        final int quantity = cursor.getInt(quantityColumnIndex);
        String displayQuantity = "Stock: " + cursor.getString(quantityColumnIndex);

        final Uri currentProductURI = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
        final long ids = cursor.getLong(cursor.getColumnIndex(ProductContract.ProductEntry._ID));

        // Update the new values to the TexViews for the current product.
        textViewName.setText(productName);
        textViewPrice.setText(displayPrice);
        textViewQuantity.setText(displayQuantity);

        // As I did in my booklisting app, I,ll use the library picasso to insert the images.

        Picasso.with(context).load(productImage)
                .placeholder(R.drawable.add_image)

                .fit()
                .into(imageViewImage);

        // We set the instructions when the user cliks the shopping button

        shoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();

                //We set a conditional statement, where if the stock value is higher than zero,
                // then decrease 1 by 1 the amount of the stock, and display the decrement
                //on to the  stock field

                if (quantity > 0) {
                    int stock = quantity;
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT, --stock);
                    resolver.update(
                            currentProductURI,
                            values,
                            null,
                            null
                    );

                    //and call the ContentResolver to notify the stock amount changes.

                    context.getContentResolver().notifyChange(currentProductURI, null);
                } else {
                    // If the stock is zero, we will aware the user
                    Toast.makeText(context, "There is not stock left, order to supplier is required ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

