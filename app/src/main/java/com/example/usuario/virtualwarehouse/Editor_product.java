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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.virtualwarehouse.data.ProductContract;

import static com.example.usuario.virtualwarehouse.R.id.product_image_spinner;

/**
 * Created by Usuario on 22/7/17.
 */

public class Editor_product extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    //Loader ID for the current product
    private static final int CURRENT_PRODUCT_LOADER_ID = 0;

    //EditText for the stock counter (entered by typing )
    public EditText productStock;


    public Spinner productImageSpinner;
    // URI content for the current product
    private Uri currentProductURI;
    // EditText for the name of the product
    private EditText productEditTextName;
    // EditText for the product price
    private EditText productEditTextPrice;
    private TextView header;

    //product image spinner
    // Product image
    private ImageView productImageView;

    //current image
    private String imageType = ProductContract.ProductEntry.IMAGE_TYPE_NONE;

    // Default URI for the current image
    private String currentImageURI = "no image";
    // Initial boolean statement to determine if the data changed
    // We initialize it a false.
    private boolean productDataChange = false;

    // We declare here a variable for the amount of deleted rows
    private int deletedRows = 0;

    // In order to check if the product has changed we use the method OnTouchListener
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productDataChange = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_product);


        // We link the variables above created with the corresponding xml Views

        productEditTextName = (EditText) findViewById(R.id.product_field_name);
        productEditTextPrice = (EditText) findViewById(R.id.product_field_price);
        productStock = (EditText) findViewById(R.id.quantity_counter);
        productImageView = (ImageView) findViewById(R.id.product_image);
        header = (TextView) findViewById(R.id.header);
        productImageSpinner = (Spinner) findViewById(R.id.product_image_spinner);

        //Check the changes for each view
        productImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                productDataChange = true;
                return false;
            }
        });

        productEditTextName.setOnTouchListener(mTouchListener);
        productEditTextPrice.setOnTouchListener(mTouchListener);
        productStock.setOnTouchListener(mTouchListener);
        productImageView.setOnTouchListener(mTouchListener);

        // We now create a new Intent with 2 available paths. We will do this through
        // a conditional statement to determine if the content is new or if it,s an update
        // from an existing product.

        Intent intent = getIntent();
        currentProductURI = intent.getData();


        // If the intent does not find a product id, that means that it,s a new product
        // in that case we will set the views for a new product.
        if (currentProductURI == null) {

            // We set a title view for " NEW PRODUCT "
            setTitle(getString(R.string.new_product_title));
            header.setVisibility(View.GONE);
            invalidateOptionsMenu();

        } else {
            // If it got an ID, it means it,s an update so we will overwrite the data, and we will set
            // a view for the title "We set the text for " MODIFY THE PRODUCT "
            setTitle(getString(R.string.modify_product));
            header.setVisibility(View.GONE);

            // We declare the URI of the current product
            Uri currentProductURI = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,
                    ContentUris.parseId(this.currentProductURI));

            // Initialize the loader in order to read the data based on the created or updated id
            getLoaderManager().initLoader(CURRENT_PRODUCT_LOADER_ID, null, this);
        }
        setImageSpinner();
    }

    //The following block of code gives the system the instructions to delete a product

    private void showDeleteConfirmationDialog() {

        // We create now an AlertDialog.Builder and set the message and set the clicks
        // for the choice

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);

        builder.setPositiveButton(R.string.delete_action, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //On click delete the current product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // On click the user cancel the delete instruction.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Method with conditional statement to delete the product

    private void deleteProduct() {

        // Only delete the product if there is actually a product
        if (currentProductURI != null) {
            // Call the ContentResolver to delete the product at the given content URI.

            int rowsDeleted = getContentResolver().delete(currentProductURI, null, null);
            if (deletedRows == 0) {
                Toast.makeText(this, R.string.delete_message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Editor_product.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.delete_error_message, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
    // This method is in order to show an emerge window message for discard changes

    private void discardChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // We create now an AlertDialog.Builder and set the message and set the clicks
        // for the choice

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.carryon_editing, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                // On click, keep editing

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Finally create and display the AlertDialog

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {

        // If the user choose to carry on editing, back to edition
        if (!productDataChange) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //On click discard, close the AlertDialog
                        finish();
                    }
                };
        // Show the dialog message saying that there are changed not saved.

        discardChangesDialog(discardButtonClickListener);
    }

    private void setImageSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout, specify the
        // layout to use when the list of choices appears and apply the adapter to the spinner.

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.array_image_type, android.R.layout.simple_spinner_item);
        productImageSpinner = (Spinner) findViewById(product_image_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productImageSpinner.setAdapter(adapter);

        // Set the integer mSelected to the constant values
        productImageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Callback method to be invoked when an item in this view has been selected.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {

                    if (selection.equals(getString(R.string.stationery))) {
                        // The tourist product is a culture product.
                        imageType = ProductContract.ProductEntry.IMAGE_STATIONERY;
                        productImageView.setImageDrawable(getDrawable(R.drawable.stationery));

                    } else if (selection.equals(getString(R.string.books))) {
                        // The tourist product is a hotel.
                        imageType = ProductContract.ProductEntry.IMAGE_BOOKS;
                        productImageView.setImageDrawable(getDrawable(R.drawable.books));

                    } else if (selection.equals(getString(R.string.presents))) {
                        // The tourist product is a leisure product.
                        imageType = ProductContract.ProductEntry.IMAGE_PRESENTS;
                        productImageView.setImageDrawable(getDrawable(R.drawable.presents));

                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined.
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                imageType = ProductContract.ProductEntry.IMAGE_TYPE_NONE;
                productImageView.setImageDrawable(getDrawable(R.drawable.add_image));
            }
        });
    }

    //The following block of code is to create a new product

    private void AddNewProduct() {

        String newName = productEditTextName.getText().toString();
        String newPrice = productEditTextPrice.getText().toString();
        String newQuantity = productStock .getText().toString();


        // If some of the fields are blanck, show a toast with a warning

        if (newName.isEmpty() || newPrice.isEmpty() || newQuantity.isEmpty()) {
            Toast.makeText(this, R.string.uncompleted, Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a content value, in order insert all the values and attributes

        ContentValues values = new ContentValues();

        values.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT, newName);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT, newPrice);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT, newQuantity);
        values.put(ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT, imageType);

        // We will understand that there is a new product, if the currentProductURI is null
        //so we set a conditional statement to instruct with the options.

        if (currentProductURI == null) {

            Uri insertedNewRow = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (insertedNewRow == null) {

                //Show a toast if the product can not be added

                Toast.makeText(this, R.string.insert_error_message, Toast.LENGTH_LONG).show();

            } else {

                //Show a toast confirmation if the product is added

                Toast.makeText(this, R.string.added_ok, Toast.LENGTH_LONG).show();
            }
        } else {

            //If the currentProductURI is not null then we need to update the existing row

            int updatedExistingRow = getContentResolver().update(currentProductURI, values, null, null);

            // if there is not null, but the variable is zero, then there is an error and we show it
            // on the toast to make the user aware

            if (updatedExistingRow == 0) {
                Toast.makeText(this, R.string.error_saving_changes_message, Toast.LENGTH_LONG).show();

                //if there is not null, and got a value, then get the intent to go to the main activity

            } else {
                Toast.makeText(this, R.string.product_edited_ok, Toast.LENGTH_LONG).show();
                Intent addedOkIntent = new Intent(this, MainActivity.class);
                startActivity(addedOkIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Lets inflate the menu options from the file edition_menu.xml

        getMenuInflater().inflate(R.menu.edition_menu, menu);
        return true;
    }

    //SWITCH the visibility of the options according of the action that
    //the user is trying to do

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // if the user is trying to create a new product ( value of currentProduct==null), then hide the delete icon.

        if (currentProductURI == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product);
            menuItem.setVisible(false);
        }
        // if it s an existing product, then set the save icon
        if (currentProductURI != null) {
            MenuItem menuItem = menu.findItem(R.id.saved_product);
            menuItem.setIcon(R.drawable.ic_save_white_24dp);
        }
        return true;
    }

    //We instruct the app now how to act according to the user choice
    //trough  "onOptionsItemSelected "

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.saved_product:
                // We saved the product and we add it straight away into the list
                AddNewProduct();
                finish();
                return true;

            case R.id.delete_product:

                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //We estimate here a projection of what we want to be searched and showed
        //by the cursor.

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT,
                ProductContract.ProductEntry.COLUMN_NAME_PRODUCT,
                ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT,
                ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT,
        };

        // Cursor loader will manage the search of the ContentProvider on a background thread

        return new CursorLoader(this,
                currentProductURI,
                projection,
                null,
                null,
                null);
    }

    // Here below, we are going to set the movements of the cursor and how it ,s gonna
    //display it

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // We indicate the columns the cursor have to move and read through

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_PRODUCT);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE_PRODUCT);

            //Now we are going to get the values of that cursor lecture into data,
            //according to the data type of each column

            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Once we get the data, we are gonna set the string, or  we are gonna "string" the value of it.

            productEditTextName.setText(name);
            productEditTextPrice.setText(String.valueOf(price));
            productStock.setText(String.valueOf(quantity));

            //Set the drawable root to the spinner in order to get the image.
            productImageView.setImageDrawable(getDrawable(getResources().getIdentifier(image, "drawable", getPackageName())));

            switch (image) {
                case ProductContract.ProductEntry.IMAGE_STATIONERY:
                    productImageSpinner.setSelection(1);
                    break;

                case ProductContract.ProductEntry.IMAGE_BOOKS:
                    productImageSpinner.setSelection(2);
                    break;

                case ProductContract.ProductEntry.IMAGE_PRESENTS:
                    productImageSpinner.setSelection(3);
                    break;

                default:
                    productImageSpinner.setSelection(0);
                    break;
            }
        }

    }

    //On reset loader, we are going to instruct the cursor to set all the values empty

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        productEditTextName.setText("");
        productEditTextPrice.setText("");
        productStock.setText("");

        // Reset image and image type spinner.
        productImageView.setImageDrawable(getDrawable(R.drawable.add_image));
        productImageSpinner.setSelection(0);

    }

}

