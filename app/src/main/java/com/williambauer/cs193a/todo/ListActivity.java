package com.williambauer.cs193a.todo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    ArrayList<String> listArr;
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initList();

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

    // initializes the list (internally and in GUI)
    private void initList() {

        if (listArr == null) {
            listArr = new ArrayList<>();
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
