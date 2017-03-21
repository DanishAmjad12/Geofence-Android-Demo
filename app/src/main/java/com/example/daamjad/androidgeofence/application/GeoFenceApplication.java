package com.example.daamjad.androidgeofence.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by daamjad on 3/14/2017.
 */

public class GeoFenceApplication extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("GeoFence")
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

    }
}
