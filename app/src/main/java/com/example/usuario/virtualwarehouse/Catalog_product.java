package com.example.usuario.virtualwarehouse;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.virtualwarehouse.data.ProductContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Usuario on 22/7/17.
 */

public class Catalog_product extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public Button increaseButton;

    public Button decreaseButton;
    //Variable for the EditText of the stock
    public EditText productStock;
    // Quantity of product in our warehouse
    private int quantity = 0;
    // requested change of stock
    private boolean requested = false;
    // URI string for the product image
    private String productImageURI = "no image";

    //Declare the variables for this activity
    //content Uri for the current product
    private Uri currentProductURI;
    //Variable for the textView of the product name
    private TextView productName;
    //Variable for the textView of the product price
    private TextView productPrice;
    //Variable for the ImageView of the product image
    private ImageView productImage;

    //Variable for the ImageView of the button for the order to the supplier
    private ImageView orderToSupplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_product);

        // Link the variables with the layouts id of the xml file (activity_catalog_product.xml)

        productName = (TextView) findViewById(R.id.product_name);
        productPrice = (TextView) findViewById(R.id.product_price);
        productStock = (EditText) findViewById(R.id.quantity_counter);
        productImage = (ImageView) findViewById(R.id.add_image);
        orderToSupplier = (ImageView) findViewById(R.id.button_order_to_supplier);

        //Link also the button with the xml file

        Button increaseButton = (Button) findViewById(R.id.button_increment);
        Button decreaseButton = (Button) findViewById(R.id.button_decrement);


        // Intent to initialize the activity in order to determine if it,s edition.
        final Intent intent = getIntent();
        currentProductURI = intent.getData();

        // Start the loader to get the data and show the results.

        getLoaderManager().initLoader(0, null, this);

        // Listener to the execute the increasing stock
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IncreaseStock();
            }
        });

        // Listener to the execute the decreasing stock.
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DecreaseStock();
            }
        });

        // Listener to order to the supplier
        orderToSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOrderSupplier();

            }
        });
    }

    // Method to update the current changed stock

    private void UpdateStock() {

        if (requested != false) {
            String amount = productStock.getText().toString();

            // We create again a Content Value in order to update the stock data

            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT, amount);

            //Now we set the conditional statement to make sure that the app updates
            //the stock, only in the case the amount of the stock has actually changed.

            if (currentProductURI != null) {

                // if the value is not null, then update
                int updatedRow = getContentResolver().update(currentProductURI, values, null, null);

                // if the value is null, then inform the user about that
                if (updatedRow == 0) {

                    Toast.makeText(this, R.string.updating_error, Toast.LENGTH_LONG).show();

                    // if the value is not null, then inform the user and go to the main activity
                } else {
                    Toast.makeText(this, R.string.saved_stock, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);

                    finish();
                }
            }
        } else {

            Toast.makeText(this, R.string.order_before_addingstock, Toast.LENGTH_LONG).show();

        }
    }

    // Method to Increase stock, invoked on the onclicklistener

    private void IncreaseStock() {

        quantity = quantity + 1;
        quantity = Integer.parseInt(productStock.getText().toString());
        int counter = quantity;
        String adding = String.valueOf(counter);
        productStock.setText(adding);

    }


    // Method to decrease stock, invoked on the onclicklistener
    private void DecreaseStock() {

        if (quantity < 0) {
            Toast.makeText(this, R.string.no_negativestock_message, Toast.LENGTH_SHORT).show();

        } else {

            quantity = quantity - 1;
            quantity = Integer.parseInt(productStock.getText().toString());
            int counter = quantity;
            String adding = String.valueOf(counter);
            productStock.setText(adding);

        }

    }

    // BLOCK OF CODE TO INTENT THE ORDER TO THE SUPPLIER BY EMAIL

    private void requestOrderSupplier() {

        String product = productName.getText().toString();

        String price = productPrice.getText().toString();

        String quantityToOrder = productStock.getText().toString();

        // Now we define a builder to include and compose the whole order

        StringBuilder builder = new StringBuilder();
        builder.append("Dear Supplier, please consider the following order: " + " :\n");
        builder.append("PRODUCT: " + product + "\n");
        builder.append("PRICE: " + price + "\n");
        builder.append("CANTIDAD: " + quantityToOrder + "\n");

        String order = builder.toString();

        // We declare the INTENT with the above variables to send the order by email

        Intent sendOrderIntent = new Intent(Intent.ACTION_SEND);
        sendOrderIntent.setData(Uri.parse("mailto:"));
        sendOrderIntent.setType("text/plain");
        sendOrderIntent.putExtra(Intent.EXTRA_SUBJECT, "Solicitud Mercancía ");
        sendOrderIntent.putExtra(Intent.EXTRA_TEXT, order);

        startActivity(sendOrderIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflates the menu options from the layout catalog_menu.xml

        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Instruct the system to act according to the
        //different menu options

        switch (item.getItemId()) {

            //If the user clicks the edit icon
            case R.id.edit_product_attributes:
                ChangeProductAttributes();

                finish();
                return true;

            // If the user clicks the save icon
            case R.id.save_stock:
                UpdateStock();
                return true;

            // If the user clicks the up navigation button
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Change the product attributes

    private void ChangeProductAttributes() {

        // Intent to release the app to the Editor activity

        Intent intent = new Intent(Catalog_product.this, Editor_product.class);

        // We declare the URI of the current product
        Uri currentProductURI = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,
                ContentUris.parseId(this.currentProductURI));

        // We set the URI to data through the corresponding method

        intent.setData(currentProductURI);
        startActivity(intent);
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // We declare a projection including all the columns

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT,
                ProductContract.ProductEntry.COLUMN_NAME_PRODUCT,
                ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT,
                ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT,
        };

        // We initialize the cursor

        return new CursorLoader(this, currentProductURI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Regresa temprano si el cursor es nulo o hay menos de 1 fila en el cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // We start to tell the cursor to move along the rows and columns

        if (cursor.moveToFirst()) {


            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT);

            // After reading all , get the data values from the columns and rows.

            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            productImageURI = cursor.getString(imageColumnIndex);

            // Set those values and display them on the layouts

            productName.setText(name);
            productPrice.setText(String.valueOf(price) + " €");
            productStock.setText(String.valueOf(quantity));

            //We update the picture through Picasso

            Picasso.with(this).load(productImageURI)
                    .placeholder(R.drawable.add_image)
                    .fit()
                    .into(productImage);
        }
    }

    // On reset we set all the fields blanck
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productName.setText("");
        productPrice.setText(String.valueOf(""));
        productStock.setText(String.valueOf(""));
    }

}
