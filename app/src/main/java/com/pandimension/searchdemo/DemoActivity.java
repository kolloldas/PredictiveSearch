/*
 * Copyright 2017 Kollol Das
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandimension.searchdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.pandimension.predictivesearch.Prediction;
import com.pandimension.predictivesearch.Predictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;


public class DemoActivity extends AppCompatActivity {
    final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    final static int MY_PERMISSIONS_REQUEST_CALL_PHONE = 200;

    private EditText mSearchBox;
    private RecyclerView mRecyclerView;
    private PredictionAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Predictor mPredictor;
    private Predictor.InputType mInputType;

    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePredictor(Predictor.InputType.NUMBER_KEYPAD);

        setContentView(R.layout.activity_demo);
        Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appToolbar);

        mSearchBox = (EditText) findViewById(R.id.editText);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mProgress = new ProgressDialog(this);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mAdapter = new PredictionAdapter(10);
        mAdapter.setOnNumberClickListener(new PredictionAdapter.OnNumberClickListener() {
            @Override
            public void onNumberClicked(String number) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));

                if (ContextCompat.checkSelfPermission(DemoActivity.this,
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                }else{
                    ActivityCompat.requestPermissions(DemoActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                }


            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Collection<Prediction> predictions = mPredictor.predict(charSequence.toString());
                if(predictions != null) {
                    mAdapter.updateDataset(predictions);
                }else{
                    // Clear the Adapter
                    mAdapter.updateDataset(new ArrayList<Prediction>());
                }
                    /* Use this function if the direct call slows down the UI */
                    /* predict(charSequence.toString()).addOnCompleteListener(DemoActivity.this,
                            new OnCompleteListener<Collection<Prediction>>() {
                        @Override
                        public void onComplete(@NonNull Task<Collection<Prediction>> task) {
                            if(task.isSuccessful()){
                                Collection<Prediction> predictions = task.getResult();
                                if(predictions != null) {
                                    mAdapter.updateDataset(predictions);
                                }else{
                                    //Clear the Adapter
                                    mAdapter.updateDataset(new ArrayList<Prediction>());
                                }
                            }
                        }
                    }); */
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED){

            loadDataUi();

        }else {
            ActivityCompat.requestPermissions(DemoActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    loadDataUi();

                } else {

                    Toast.makeText(DemoActivity.this,
                            "Cannot work without READ_CONTACTS permission",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(DemoActivity.this,
                            "Tap again to call",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem qwerty = menu.findItem(R.id.action_qwerty);
        MenuItem num = menu.findItem(R.id.action_numpad);
        // Check the appropriate item based on input type
        qwerty.setChecked(mInputType == Predictor.InputType.QWERTY_KEYPAD);
        num.setChecked(mInputType == Predictor.InputType.NUMBER_KEYPAD);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_qwerty:
                if(switchInput(Predictor.InputType.QWERTY_KEYPAD)) {
                    item.setChecked(true);
                }
                return true;
            case R.id.action_numpad:
                if(switchInput(Predictor.InputType.NUMBER_KEYPAD))
                    item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize the Predictor
     * @param inputType Type of input to use
     */
    void initializePredictor(Predictor.InputType inputType){
        mPredictor = new Predictor(inputType);
        mPredictor.setLabels(SimpleDataItem.getLabels());
        mInputType = inputType;
    }

    /**
     * Switch input types between NUMPAD and QWERTY
     * @param newInputType The input type to change to
     * @return True if input type wa changed. False otherwise
     */
    boolean switchInput(Predictor.InputType newInputType){
        if(mInputType != newInputType){
            // Clear Adapter
            mAdapter.updateDataset(new ArrayList<Prediction>());
            // Initialize predictor with new input type
            initializePredictor(newInputType);
            // Load the data showing loading indicator
            loadDataUi();
            return true;
        }
        return false;
    }

    /**
     * Load data with Loading ProgressBar and post processing
     */
    void loadDataUi(){
        mProgress.setMessage("Loading..");
        mProgress.show();
        loadData().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            if(task.isComplete()){
                mProgress.dismiss();
            }
            // Clear
            mSearchBox.setText("");
            // Set the search box input method type according to Predictor input type
            mSearchBox.setInputType(mInputType == Predictor.InputType.NUMBER_KEYPAD ?
                    InputType.TYPE_CLASS_PHONE: InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        });
    }

    /**
     * Method to load contacts data. Using GMS tasks to make our lives easier. Could run the
     * task on a ThreadPoolExecutor also, but this is just a lame demo :)
     * @return A task indicating success/failure
     */
    Task<Void> loadData(){
        final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);

                if(cursor != null) {
                    int indexId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    int indexName = cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                    // This is quite a slow operation hence it's recommended that you cache the
                    // Contact data in an internal db and load from there. The internal db
                    // can be synced based upon any changes to the Contacts
                    while(cursor.moveToNext()){
                        String id = cursor.getString(indexId);
                        String name = cursor.getString(indexName);


                        // Query all the numbers and add it in one call. We're ignoring phone type
                        // but you can create that as a field in the implementation of DataItem
                        if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                            Cursor cursor2 = getContentResolver().
                                    query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                          ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                          new String[]{id}, null);

                            int indexPhone = cursor2.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER);

                            LinkedList<String> numbers = new LinkedList<String>();

                            while (cursor2.moveToNext()) {
                                numbers.add(cursor2.getString(indexPhone));

                            }
                            cursor2.close();
                            mPredictor.addItem(new SimpleDataItem(name, numbers, id));
                        }


                    }
                    cursor.close();

                }

                source.setResult(null);
            }
        }).start();

        return source.getTask();
    }

    /**
     * Run prediction on a separate task. Again using GMS tasks API
     * @param query The query to run prediction on
     * @return
     */
    Task<Collection<Prediction>> predict(final String query){
        final TaskCompletionSource<Collection<Prediction>> source = new TaskCompletionSource<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Collection<Prediction> predictions = mPredictor.predict(query);
                source.setResult(predictions);
            }
        }).start();

        return source.getTask();
    }
}
