package com.williambauer.cs193a.todo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class ListActivity extends AppCompatActivity {
    private static final String LIST_FILENAME = "listFile.txt";

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
        listArr.add(addEditText.getText().toString());
        addEditText.setText("");
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

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("listArr", listArr);
    }

    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        listArr = inState.getStringArrayList("listArr");
        initList();
    }

}
