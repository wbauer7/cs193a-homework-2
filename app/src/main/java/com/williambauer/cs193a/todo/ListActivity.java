package com.williambauer.cs193a.todo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ListActivity extends AppCompatActivity {
    private static final String LIST_FILENAME = "listFile.txt";
    private static final String SAVE_URL = "http://cs193a.williambauer.com/todo/save.php";
    private static final String LOAD_URL = "http://cs193a.williambauer.com/todo/load.php";

    ArrayList<String> listArr;
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // add a longClick listener to remove items
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int index, long id) {
                        listArr.remove(index);
                        listAdapter.notifyDataSetChanged();
                        return false;
                    }
                }
        );

        // ******
        try {
            URL url = new URL(LOAD_URL);
            new ReadListFromServerTask().execute(url);
        } catch (IOException ioe) {
            Log.e("SaveListToServerCall", ioe.toString());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        initList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveListToFile();
    }

    // initializes the list (internally and in GUI)
    private void initList() {

        if (listArr == null) {
            listArr = new ArrayList<>();
            readListFromFile();
        }

        listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listArr);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
    }

    public void addItem(View view) {
        EditText addEditText = (EditText) findViewById(R.id.addEditText);
        addItem(addEditText.getText().toString());
        addEditText.setText("");
    }

    private void addItem(String text) {
        listArr.add(text);
        listAdapter.notifyDataSetChanged();
    }

    // saves the contents of the list to persistent storage
    // Adapted from assignment spec and Marty's lecture notes
    private void saveListToFile() {
        try {
            PrintStream out = new PrintStream(
                    openFileOutput(LIST_FILENAME, MODE_PRIVATE)
            );

            for (int i = 0; i < listArr.size(); i++) {
                out.println(listArr.get(i));
            }

            out.close();

        } catch (IOException ioe) {
            Log.e("saveListToFile", ioe.toString());
        }
    }

    // reads in a list from persistent storage
    // does not preserve internal list
    // Adapted from assignment spec and Marty's lecture notes
    private void readListFromFile() {
        // clear existing list
        if (listArr == null || listArr.size() > 0) {
            listArr = new ArrayList<>();
        }

        try {
            Scanner scan = new Scanner(openFileInput(LIST_FILENAME));

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                listArr.add(line);
            }

            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }

        } catch (IOException ioe) {
            Log.e("ReadListFromFile", ioe.toString());
        }

    }

    // saves the contents of the list to a web server
    // http://developer.android.com/reference/java/net/HttpURLConnection.html
//    private void saveListToServer() {



//        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//        try {
//            urlConnection.setDoOutput(true);
//            urlConnection.setChunkedStreamingMode(0);
//
//            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//            writeStream(out);
//
//            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//            readStream(in);
//            finally {
//                urlConnection.disconnect();
//            }
//        }


//    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("listArr", listArr);
    }

    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        listArr = inState.getStringArrayList("listArr");
        initList();
    }


    private class SaveListToServerTask extends AsyncTask<URL, Long, String> {

        protected String doInBackground(URL... urls) {
            StringBuilder total = new StringBuilder();

            int count = urls.length;
            long totalSize = 0;
            for (URL url : urls) {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    // read whole stream
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    String line;

                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }

                    urlConnection.disconnect();

                } catch (IOException ioe) {
                    Log.e("saveListToServer", ioe.toString());
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return total.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
//            showDialog("Downloaded " + result + " bytes");
//            Toast.makeText(ListActivity.this, result, Toast.LENGTH_LONG);
            addItem(result);
        }
    }


    private class ReadListFromServerTask extends AsyncTask<URL, Long, String> {

        protected String doInBackground(URL... urls) {
            StringBuilder total = new StringBuilder();

            int count = urls.length;
            long totalSize = 0;
            for (URL url : urls) {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    // read whole stream
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    String line;

                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }

                    urlConnection.disconnect();

                } catch (IOException ioe) {
                    Log.e("saveListToServer", ioe.toString());
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return total.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
//            showDialog("Downloaded " + result + " bytes");
//            Toast.makeText(ListActivity.this, result, Toast.LENGTH_LONG);
            addItem(result);
        }
    }

}

