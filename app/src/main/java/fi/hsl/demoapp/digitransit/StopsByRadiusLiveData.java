package fi.hsl.demoapp.digitransit;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import fi.hsl.digitransit.DigitransitAPI;
import fi.hsl.digitransit.domain.StopAtDistance;
import fi.hsl.digitransit.domain.StopAtDistanceEdge;

public class StopsByRadiusLiveData extends LiveData<List<StopAtDistance>> {

    public StopsByRadiusLiveData(final DigitransitAPI digitransitAPI, final double latitude, final double longitude, final int radius) {
        new AsyncTask<Void, Void, List<StopAtDistance>>() {

            @Override
            protected List<StopAtDistance> doInBackground(Void... voids) {
                try {
                    List<StopAtDistanceEdge> stopAtDistanceEdges = digitransitAPI.queryStopsByLocation(latitude, longitude, radius).getEdges();
                    List<StopAtDistance> stopsAtDistance = new ArrayList<>(stopAtDistanceEdges.size());
                    for (StopAtDistanceEdge edge : stopAtDistanceEdges) {
                        stopsAtDistance.add(edge.getNode());
                    }

                    return stopsAtDistance;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<StopAtDistance> stopsAtDistance) {
                setValue(stopsAtDistance);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
