package fi.hsl.demoapp.digitransit;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.Optional;

import fi.hsl.demoapp.util.AsyncTaskResult;
import fi.hsl.digitransit.DigitransitAPI;
import fi.hsl.digitransit.domain.Stop;
import fi.hsl.digitransit.domain.StopAtDistance;

public class StopLiveData extends LiveData<AsyncTaskResult<Stop>> {
    private DigitransitAPI digitransitAPI;

    public StopLiveData(final DigitransitAPI digitransitAPI) {
        this.digitransitAPI = digitransitAPI;
    }

    public void loadStop(final String gtfsId) {
        new AsyncTask<Void, Void, AsyncTaskResult<Stop>>() {

            @Override
            protected AsyncTaskResult<Stop> doInBackground(Void... voids) {
                try {
                    return new AsyncTaskResult(digitransitAPI.queryStopById(gtfsId));
                } catch (Exception e) {
                    return new AsyncTaskResult(e);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Stop> result) {
                setValue(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
