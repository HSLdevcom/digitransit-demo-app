package fi.hsl.demoapp;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.List;

import fi.hsl.demoapp.digitransit.StopsByRadiusLiveData;
import fi.hsl.demoapp.location.LocationLiveData;
import fi.hsl.digitransit.DigitransitAPI;
import fi.hsl.digitransit.GraphQLDigitransitAPI;
import fi.hsl.digitransit.domain.Stop;
import fi.hsl.digitransit.domain.StopAtDistance;
import fi.hsl.digitransit.domain.StopAtDistanceConnection;
import okhttp3.OkHttpClient;

public class MainViewModel extends AndroidViewModel {
    private LocationLiveData locationLiveData;
    private LiveData<List<StopAtDistance>> stopLiveData;

    //TODO: dependency injection
    private DigitransitAPI digitransitAPI;

    public MainViewModel(@NonNull Application application) {
        super(application);

        digitransitAPI = new GraphQLDigitransitAPI(new OkHttpClient());

        locationLiveData = new LocationLiveData(application);
        stopLiveData = Transformations.switchMap(locationLiveData, new Function<Location, LiveData<List<StopAtDistance>>>() {
            @Override
            public LiveData<List<StopAtDistance>> apply(Location input) {
                return new StopsByRadiusLiveData(digitransitAPI, input.getLatitude(), input.getLongitude(), 500);
            }
        });
    }

    public LiveData<List<StopAtDistance>> getStops() {
        return stopLiveData;
    }

    public boolean isDataRequested() {
        return locationLiveData.isLocationRequested();
    }

    public void requestData() {
        locationLiveData.requestLocation();
    }
}
