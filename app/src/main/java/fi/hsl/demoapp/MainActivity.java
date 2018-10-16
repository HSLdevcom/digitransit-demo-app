package fi.hsl.demoapp;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fi.hsl.demoapp.util.OnItemClickListener;
import fi.hsl.demoapp.util.SpaceItemDecoration;
import fi.hsl.demoapp.util.ViewState;
import fi.hsl.digitransit.domain.StopAtDistance;

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
                //Don't start refresh if already refreshing
                if (viewModel.getState().getValue().state == ViewState.State.REFRESHING) {
                    return;
                }

                viewModel.refresh();
            }
        });

        stopsView = findViewById(R.id.stops);
        stopsView.setHasFixedSize(true);
        stopsView.setLayoutManager(new LinearLayoutManager(this));
        stopsView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_view_space)));
        stopsAdapter = new StopAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(
                        StopActivity.createIntent(MainActivity.this, stopsAdapter
                                                                                .getItem(position)
                                                                                .getStop()
                                                                                .getGtfsId()));
            }
        });
        stopsView.setAdapter(stopsAdapter);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getState().observe(this, new Observer<ViewState<List<StopAtDistance>>>() {
            @Override
            public void onChanged(@Nullable ViewState<List<StopAtDistance>> viewState) {
                if (viewState.state == ViewState.State.LOADING) {
                    progress.setVisibility(View.VISIBLE);
                    swipeRefresh.setVisibility(View.INVISIBLE);
                    swipeRefresh.setEnabled(false);
                } else if (viewState.state == ViewState.State.CONTENT) {
                    progress.setVisibility(View.INVISIBLE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    swipeRefresh.setEnabled(true);
                    swipeRefresh.setRefreshing(false);

                    stopsAdapter.setData(viewState.content);
                } else if (viewState.state == ViewState.State.REFRESHING) {
                    progress.setVisibility(View.INVISIBLE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    //swipeRefresh.setEnabled(false);
                    swipeRefresh.setRefreshing(true);

                    if (stopsAdapter.getItemCount() == 0) {
                        stopsAdapter.setData(viewState.content);
                    }
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

        private OnItemClickListener itemClickListener;

        public StopAdapter(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;

            viewPool.setMaxRecycledViews(0, 30);
        }

        public void setData(List<StopAtDistance> stops) {
            this.stops.clear();
            this.stops.addAll(stops);
            notifyDataSetChanged();
        }

        public StopAtDistance getItem(int position) {
            return stops.get(position);
        }

        @NonNull
        @Override
        public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            StopViewHolder stopViewHolder = new StopViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.stop, parent, false), itemClickListener);

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

            public StopViewHolder(View itemView, final OnItemClickListener onItemClickListener) {
                super(itemView);
                //TODO: capture clicks from whole view
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                });

                name = itemView.findViewById(R.id.name);
                distance = itemView.findViewById(R.id.distance);
                departures = itemView.findViewById(R.id.departures);
            }
        }
    }
}
