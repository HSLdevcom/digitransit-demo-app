package fi.hsl.demoapp;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import fi.hsl.demoapp.digitransit.StopLiveData;
import fi.hsl.demoapp.util.AsyncTaskResult;
import fi.hsl.demoapp.util.ViewState;
import fi.hsl.digitransit.DigitransitAPI;
import fi.hsl.digitransit.GraphQLDigitransitAPI;
import fi.hsl.digitransit.domain.Stop;
import okhttp3.OkHttpClient;

public class StopViewModel extends AndroidViewModel {
    private MediatorLiveData<ViewState<Stop>> viewState;

    private String gtfsId;

    private StopLiveData stopLiveData;

    //TODO: dependency injection
    private DigitransitAPI digitransitAPI;

    public StopViewModel(@NonNull Application application) {
        super(application);

        digitransitAPI = new GraphQLDigitransitAPI(new OkHttpClient());

        viewState = new MediatorLiveData<>();

        stopLiveData = new StopLiveData(digitransitAPI);
        LiveData<ViewState> liveData = Transformations.map(stopLiveData, new Function<AsyncTaskResult<Stop>, ViewState>() {
            @Override
            public ViewState apply(AsyncTaskResult<Stop> input) {
                if (input.getResult() != null) {
                    return ViewState.content(input.getResult());
                } else {
                    return ViewState.error(input.getError().getMessage());
                }
            }
        });
        viewState.addSource(liveData, new Observer<ViewState>() {
            @Override
            public void onChanged(@Nullable ViewState vs) {
                viewState.setValue(vs);
            }
        });
    }

    public void setStopId(String gtfsId) {
        if (viewState.getValue() == null) {
            this.gtfsId = gtfsId;

            viewState.setValue(ViewState.loading());
            stopLiveData.loadStop(gtfsId);
        }
    }

    public void refresh() {
        if (viewState.getValue().state == ViewState.State.CONTENT) {
            viewState.setValue(ViewState.refreshing(viewState.getValue().content));
            stopLiveData.loadStop(gtfsId);
        }
    }

    public LiveData<ViewState<Stop>> getState() {
        return viewState;
    }
}
