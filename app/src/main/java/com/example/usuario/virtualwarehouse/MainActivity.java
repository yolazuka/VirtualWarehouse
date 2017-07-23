package com.example.usuario.virtualwarehouse;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.usuario.virtualwarehouse.data.ProductContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // We declare the adapter

    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the button with the view xml

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // Find the ListView where the list of products will be listed.
        ListView productListView = (ListView) findViewById(R.id.list_products);

        // Escucha botón flotante para agregar un nuevo producto.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Editor_product.class);
                startActivity(intent);
            }
        });

        // Set the view empty view only when the list of items be empty

        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Set an adapter for each line of items
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);


        // Listener to redirect the item to the Catalog_product activity

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, Catalog_product.class);

                // Gather the final uri, linking the Content uri + the uri of the current product.

                Uri createdProductURI = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                //Set the new final URI
                intent.setData(createdProductURI);

                startActivity(intent);
            }
        });
        // We initialize now the loader.
        getLoaderManager().initLoader(0, null, this);
    }

    //Method to insert new dummy data

    private void addDummyData() {

        // We define a Content Value with fake info.
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT, "Dummy Product");
        values.put(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT, "20");
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT, 30);

        // Through the new uri and the contentResolver we insert the data into the table.

        Uri dummyUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

    }

    // We create a method to instruct and set a pop-up asking the user for the delete confirmation.

    private void showDeleteConfirmationPopUp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_confirmation);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // delete them all if the user clicks this option

                deleteAllProducts();
                Toast.makeText(MainActivity.this, R.string.deleted_list, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // dont delete anything if the user clicks this option, so dismiss the action
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // With these below methods the system creates and show the "pop-up"
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("DELETE LIST");
        alertDialog.show();
    }

    @Override


    // Now we are going to instruct similar behavior for the dummy data //

    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu when created data
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    private void deleteAllProducts() {
        int deletedRows = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);

    }

    @Override

    //Let,s switch the cases according to the user choice

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // If the user clicks add dummy data, add it
            case R.id.insert_dummy:
                addDummyData();
                return true;


            // // If the user clicks delete all, delete them all
            case R.id.delete_all:
                showDeleteConfirmationPopUp();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Now insert the projection through the cursor in order to execute the query search in a backgroun thread

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME_PRODUCT,
                ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT,
                ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT,
                ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT,
        };

        return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    public void clickOnViewItem(long id) {
        Intent intent = new Intent(this, Editor_product.class);
        intent.putExtra("itemId", id);
        startActivity(intent);
    }

    @Override

    // with this method we will update the ProductCursorAdapter
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
