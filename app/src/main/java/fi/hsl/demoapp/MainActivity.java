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
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
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
import java.util.Objects;

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
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.refresh();
            }
        });

        stopsView = findViewById(R.id.stops);
        stopsView.setHasFixedSize(true);
        stopsView.setLayoutManager(new LinearLayoutManager(this));
        stopsView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_view_space)));
        stopsAdapter = new StopAdapter();
        stopsView.setAdapter(stopsAdapter);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getState().observe(this, new Observer<MainViewModel.ViewState>() {
            @Override
            public void onChanged(@Nullable MainViewModel.ViewState viewState) {
                if (viewState.state == MainViewModel.ViewState.State.LOADING) {
                    progress.setVisibility(View.VISIBLE);
                    swipeRefresh.setVisibility(View.INVISIBLE);
                    swipeRefresh.setEnabled(false);
                } else if (viewState.state == MainViewModel.ViewState.State.CONTENT) {
                    progress.setVisibility(View.INVISIBLE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    swipeRefresh.setEnabled(true);
                    swipeRefresh.setRefreshing(false);

                    stopsAdapter.setData(viewState.content);
                } else if (viewState.state == MainViewModel.ViewState.State.REFRESHING) {
                    progress.setVisibility(View.INVISIBLE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    swipeRefresh.setEnabled(false);
                    swipeRefresh.setRefreshing(true);

                    stopsAdapter.setData(viewState.content);
                }
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.notifyPermissionGranted();
        } else {
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.notifyPermissionGranted();
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

        public StopAdapter() {
            viewPool.setMaxRecycledViews(0, 30);
        }

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
            LinearLayoutManager llm = new LinearLayoutManager(stopViewHolder.departures.getContext());
            llm.setInitialPrefetchItemCount(5);
            stopViewHolder.departures.setLayoutManager(llm);
            stopViewHolder.departures.setAdapter(new DepartureAdapter());

            return stopViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
            StopAtDistance stopAtDistance = stops.get(position);

            holder.name.setText(stopAtDistance.getStop().getName());
            holder.distance.setText(String.format("%dm", stopAtDistance.getDistance()));
            ((DepartureAdapter)holder.departures.getAdapter()).submitList(stopAtDistance.getStop().getStoptimesWithoutPatterns());
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

    private static class DepartureAdapter extends ListAdapter<Stoptime, DepartureAdapter.DepartureViewHolder> {
        public DepartureAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public DepartureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DepartureViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.departure, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DepartureViewHolder holder, int position) {
            Stoptime stoptime = getItem(position);

            //Stoptimes are in seconds but DateFormat class uses milliseconds
            holder.time.setText(DateFormat.getTimeFormat(holder.time.getContext()).format((stoptime.getScheduledDeparture() + stoptime.getServiceDay()) * 1000));
            holder.route.setText(stoptime.getTrip().getRoute().getShortName());
            holder.headsign.setText(stoptime.getHeadsign());
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

        public static final DiffUtil.ItemCallback<Stoptime> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<Stoptime>() {

                    @Override
                    public boolean areItemsTheSame(Stoptime oldItem, Stoptime newItem) {
                        return Objects.equals(oldItem.getTrip().getGtfsId(), newItem.getTrip().getGtfsId());
                    }

                    @Override
                    public boolean areContentsTheSame(Stoptime oldItem, Stoptime newItem) {
                        return Objects.equals(oldItem, newItem);
                    }
                };
    }
}
