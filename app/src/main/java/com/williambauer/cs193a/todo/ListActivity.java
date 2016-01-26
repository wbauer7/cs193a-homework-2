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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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


    }

    @Override
    protected void onStart() {
        super.onStart();
        loadList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveListToFile();
        saveListToServer();
    }


    private void loadList() {
        if (listArr == null) {
            listArr = new ArrayList<>();

            try {
                URL url = new URL(LOAD_URL);
                new ReadListFromServerTask().execute(url);
            } catch (IOException ioe) {
                Log.e("ReadListFromServerCall", ioe.toString());
            }
        }
    }


    private void initListAdapter() {

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

    private void saveListToServer() {
        try {
                URL url = new URL(SAVE_URL);
                new SaveListToServerTask().execute(url);
        } catch (IOException ioe) {
            Log.e("SaveListToServerCall", ioe.toString());
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
        loadList();
    }


    private class SaveListToServerTask extends AsyncTask<URL, Long, String> {

        protected String doInBackground(URL... urls) {
            StringBuilder total = new StringBuilder();

            int count = urls.length;
            long totalSize = 0;
            for (URL url : urls) {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setDoOutput(true);
//                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

                    // convert list to text format
                    String listArrStr = listArr.toString().substring(1, listArr.toString().length() - 1);
                    listArrStr = listArrStr.replaceAll(", ", "\n");
                    listArrStr = listArrStr.replaceAll(",", "\n");

                    Map<String, String> params = new HashMap<>();
                    params.put("list", listArrStr);
                    String paramString = createQueryStringForParameters(params);

//                    Log.d("dbg", listArrStr);

                    urlConnection.setFixedLengthStreamingMode(
                            paramString.getBytes().length);

                    // send the output
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out));
                    wr.write(paramString);
                    wr.close();

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

        // https://ihofmann.wordpress.com/2013/01/23/android-sending-post-requests-with-parameters/
        private static final char PARAMETER_DELIMITER = '&';
        private static final char PARAMETER_EQUALS_CHAR = '=';
        private String createQueryStringForParameters(Map<String, String> parameters) {
            StringBuilder parametersAsQueryString = new StringBuilder();
            if (parameters != null) {
                boolean firstParameter = true;

                for (String parameterName : parameters.keySet()) {
                    if (!firstParameter) {
                        parametersAsQueryString.append(PARAMETER_DELIMITER);
                    }

                    parametersAsQueryString.append(parameterName)
                            .append(PARAMETER_EQUALS_CHAR)
                            .append(URLEncoder.encode(
                                    parameters.get(parameterName)));

                    firstParameter = false;
                }
            }

            return parametersAsQueryString.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            // do nothing?
        }
    }


    private class ReadListFromServerTask extends AsyncTask<URL, Long, ArrayList<String>> {
        private IOException ioException = null;

        protected ArrayList<String> doInBackground(URL... urls) {
            ArrayList<String> list = new ArrayList<>();

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
                        list.add(line);
                    }

                    urlConnection.disconnect();

                } catch (IOException ioe) {
                    Log.e("readListFromServer", ioe.toString());
                    ioException = ioe;
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return list;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(ArrayList<String> result) {

            // if list was downloaded successfully
            if (ioException == null) {
                listArr = result;
            } else {
                readListFromFile();
            }

            initListAdapter();

        }
    }

}

