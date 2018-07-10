package fi.hsl.demoapp;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fi.hsl.demoapp.util.SpaceItemDecoration;
import fi.hsl.digitransit.domain.StopAtDistance;
import fi.hsl.digitransit.domain.Stoptime;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private MainViewModel viewModel;

    private ProgressBar progress;

    private SwipeRefreshLayout swipeRefresh;

    private RecyclerView stopsView;
    private StopAdapter stopsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.progress);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setEnabled(false);

        stopsView = findViewById(R.id.stops);
        stopsView.setHasFixedSize(true);
        stopsView.setLayoutManager(new LinearLayoutManager(this));
        stopsView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_view_space)));
        stopsAdapter = new StopAdapter();
        stopsView.setAdapter(stopsAdapter);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getStops().observe(this, new Observer<List<StopAtDistance>>() {
            @Override
            public void onChanged(@Nullable List<StopAtDistance> stopAtDistances) {
                progress.setVisibility(View.INVISIBLE);

                stopsAdapter.setData(stopAtDistances);
            }
        });

        if (!viewModel.isDataRequested()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                viewModel.requestData();
            } else {
                requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.requestData();
                } else {
                    //TODO: handle permission denial
                }
                break;
            default:
                break;
        }
    }

    private static class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {
        //View pool for nested RecyclerViews
        private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

        private List<StopAtDistance> stops = new ArrayList<>();

        public void setData(List<StopAtDistance> stops) {
            this.stops.clear();
            this.stops.addAll(stops);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            StopViewHolder stopViewHolder = new StopViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.stop, parent, false));

            stopViewHolder.departures.setRecycledViewPool(viewPool);
            stopViewHolder.departures.setLayoutManager(new LinearLayoutManager(stopViewHolder.departures.getContext()));
            stopViewHolder.departures.setAdapter(new DepartureAdapter());

            return stopViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
            StopAtDistance stopAtDistance = stops.get(position);

            holder.name.setText(stopAtDistance.getStop().getName());
            holder.distance.setText(String.format("%dm", stopAtDistance.getDistance()));
            ((DepartureAdapter)holder.departures.getAdapter()).setData(stopAtDistance.getStop().getStoptimesWithoutPatterns());
        }

        @Override
        public int getItemCount() {
            return stops.size();
        }

        public static class StopViewHolder extends RecyclerView.ViewHolder {
            public final TextView name;
            public final TextView distance;
            public final RecyclerView departures;

            public StopViewHolder(View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.name);
                distance = itemView.findViewById(R.id.distance);
                departures = itemView.findViewById(R.id.departures);
            }
        }
    }

    private static class DepartureAdapter extends RecyclerView.Adapter<DepartureAdapter.DepartureViewHolder> {
        private List<Stoptime> stoptimes = new ArrayList<>();

        public void setData(List<Stoptime> stoptimes) {
            this.stoptimes.clear();
            this.stoptimes.addAll(stoptimes);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DepartureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DepartureViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.departure, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DepartureViewHolder holder, int position) {
            Stoptime stoptime = stoptimes.get(position);

            //Stoptimes are in seconds but DateFormat class uses milliseconds
            holder.time.setText(DateFormat.getTimeFormat(holder.time.getContext()).format((stoptime.getScheduledDeparture() + stoptime.getServiceDay()) * 1000));
            holder.route.setText(stoptime.getTrip().getRoute().getShortName());
            holder.headsign.setText(stoptime.getHeadsign());
        }

        @Override
        public int getItemCount() {
            return stoptimes.size();
        }

        public static class DepartureViewHolder extends RecyclerView.ViewHolder {
            public final TextView time;
            public final TextView route;
            public final TextView headsign;

            public DepartureViewHolder(View itemView) {
                super(itemView);

                time = itemView.findViewById(R.id.time);
                route = itemView.findViewById(R.id.route);
                headsign = itemView.findViewById(R.id.headsign);
            }
        }
    }
}
