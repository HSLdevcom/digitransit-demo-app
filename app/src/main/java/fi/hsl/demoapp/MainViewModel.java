package fi.hsl.demoapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import fi.hsl.demoapp.location.LocationLiveData;

public class MainViewModel extends AndroidViewModel {
    private LocationLiveData locationLiveData;


    public MainViewModel(@NonNull Application application) {
        super(application);

        locationLiveData = new LocationLiveData(application);
    }

    public LocationLiveData getLocation() {
        return locationLiveData;
    }
}
