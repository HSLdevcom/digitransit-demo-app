package fi.hsl.demoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.view.View;

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
    private MediatorLiveData<ViewState> viewState;

    //Live data for location update
    private LocationLiveData locationLiveData;

    //TODO: dependency injection
    private DigitransitAPI digitransitAPI;

    public MainViewModel(@NonNull Application application) {
        super(application);

        digitransitAPI = new GraphQLDigitransitAPI(new OkHttpClient());

        viewState = new MediatorLiveData<>();

        locationLiveData = new LocationLiveData(getApplication());
        LiveData<ViewState> stopLiveData = Transformations.map(Transformations.switchMap(locationLiveData, new Function<Location, LiveData<List<StopAtDistance>>>() {
            @Override
            public LiveData<List<StopAtDistance>> apply(Location input) {
                return new StopsByRadiusLiveData(digitransitAPI, input.getLatitude(), input.getLongitude(), 500);
            }
        }), new Function<List<StopAtDistance>, ViewState>() {
            @Override
            public ViewState apply(List<StopAtDistance> input) {
                return ViewState.content(input);
            }
        });
        viewState.addSource(stopLiveData, new Observer<ViewState>() {
            @Override
            public void onChanged(@Nullable ViewState vs) {
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

    public LiveData<ViewState> getState() {
        return viewState;
    }

    public static class ViewState {
        public enum State { LOADING, REFRESHING, CONTENT, ERROR }

        public State state;
        public List<StopAtDistance> content;
        public String error;

        private ViewState(State state, List<StopAtDistance> content, String error) {
            this.state = state;
            this.content = content;
            this.error = error;
        }

        public static ViewState loading() {
            return new ViewState(State.LOADING, null, null);
        }

        public static ViewState refreshing(List<StopAtDistance> content) {
            return new ViewState(State.REFRESHING, content, null);
        }

        public static ViewState content(List<StopAtDistance> content) {
            return new ViewState(State.CONTENT, content, null);
        }

        public static ViewState error(String error) {
            return new ViewState(State.ERROR, null, error);
        }
    }
}
