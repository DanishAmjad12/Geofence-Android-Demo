package com.example.daamjad.androidgeofence.models;

import io.realm.RealmObject;

/**
 * Created by daamjad on 3/14/2017.
 */

public class AddTasks extends RealmObject {

    private String task;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
