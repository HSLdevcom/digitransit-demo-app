package fi.hsl.demoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.List;

import fi.hsl.demoapp.digitransit.StopsByRadiusLiveData;
import fi.hsl.demoapp.location.LocationLiveData;
import fi.hsl.demoapp.util.ViewState;
import fi.hsl.digitransit.DigitransitAPI;
import fi.hsl.digitransit.GraphQLDigitransitAPI;
import fi.hsl.digitransit.domain.StopAtDistance;
import okhttp3.OkHttpClient;

public class MainViewModel extends AndroidViewModel {
    private MediatorLiveData<ViewState<List<StopAtDistance>>> viewState;

    //Live data for location update
    private LocationLiveData locationLiveData;

    //TODO: dependency injection
    private DigitransitAPI digitransitAPI;

    public MainViewModel(@NonNull Application application) {
        super(application);

        digitransitAPI = new GraphQLDigitransitAPI(new OkHttpClient());

        viewState = new MediatorLiveData<>();

        locationLiveData = new LocationLiveData(getApplication());
        LiveData<ViewState<List<StopAtDistance>>> stopLiveData = Transformations.map(Transformations.switchMap(locationLiveData, new Function<Location, LiveData<List<StopAtDistance>>>() {
            @Override
            public LiveData<List<StopAtDistance>> apply(Location input) {
                return new StopsByRadiusLiveData(digitransitAPI, input.getLatitude(), input.getLongitude(), 500);
            }
        }), new Function<List<StopAtDistance>, ViewState<List<StopAtDistance>>>() {
            @Override
            public ViewState<List<StopAtDistance>> apply(List<StopAtDistance> input) {
                return ViewState.content(input);
            }
        });
        viewState.addSource(stopLiveData, new Observer<ViewState<List<StopAtDistance>>>() {
            @Override
            public void onChanged(@Nullable ViewState<List<StopAtDistance>> vs) {
                viewState.setValue(vs);
            }
        });
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void notifyPermissionGranted() {
        if (viewState.getValue() == null) {
            viewState.setValue(ViewState.loading());
            locationLiveData.requestLocation();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void refresh() {
        if (viewState.getValue().state == ViewState.State.CONTENT) {
            viewState.setValue(ViewState.refreshing(viewState.getValue().content));
            locationLiveData.requestLocation();
        }
    }

    public LiveData<ViewState<List<StopAtDistance>>> getState() {
        return viewState;
    }
}
