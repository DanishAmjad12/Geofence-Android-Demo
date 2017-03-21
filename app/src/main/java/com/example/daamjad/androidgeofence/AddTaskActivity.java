package com.example.daamjad.androidgeofence;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daamjad.androidgeofence.models.AddTasks;

import io.realm.Realm;

/**
 * Created by daamjad on 3/14/2017.
 */

public class AddTaskActivity extends AppCompatActivity {

    private static final String TAG = "AddTask";
    private Realm realm;
    private EditText editTextTasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editTextTasks = (EditText) findViewById(R.id.editTextTask);

        getSupportActionBar().setTitle("Add Tasks");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_task_done:
                if (!TextUtils.isEmpty(editTextTasks.getText())) {
                    addTaskInToDb();
                } else {
                    Toast.makeText(this, "Please add the task", Toast.LENGTH_SHORT).show();
                }
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addTaskInToDb() {
        AddTasks addTasks = new AddTasks();
        addTasks.setTask(editTextTasks.getText().toString());

        realm.beginTransaction();
        realm.copyToRealm(addTasks);
        realm.commitTransaction();
        Toast.makeText(this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
        setResult(200);
        finish();
    }
}
