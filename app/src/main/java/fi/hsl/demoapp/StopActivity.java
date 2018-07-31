package fi.hsl.demoapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import fi.hsl.demoapp.util.SpaceItemDecoration;

public class StopActivity extends AppCompatActivity {
    private static final String GTFS_ID = "gtfs_id";

    public static Intent createIntent(Context context, String gtfsId) {
        Intent intent = new Intent(context, StopActivity.class);
        intent.putExtra(GTFS_ID, gtfsId);

        return intent;
    }

    private StopViewModel viewModel;

    private ProgressBar progress;

    private SwipeRefreshLayout swipeRefresh;

    private RecyclerView departuresView;
    private DepartureAdapter departureAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);

        progress = findViewById(R.id.progress);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Don't start refresh if already refreshing
                if (viewModel.getState().getValue().state == StopViewModel.ViewState.State.REFRESHING) {
                    return;
                }

                viewModel.refresh();
            }
        });

        departuresView = findViewById(R.id.departures);
        departuresView.setHasFixedSize(true);
        departuresView.setLayoutManager(new LinearLayoutManager(this));
        departuresView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_view_space)));
        departureAdapter = new DepartureAdapter();
        departuresView.setAdapter(departureAdapter);

        viewModel = ViewModelProviders.of(this).get(StopViewModel.class);

        //Set stop id to load
        viewModel.setStopId(getIntent().getStringExtra(GTFS_ID));

        viewModel.getState().observe(this, new Observer<StopViewModel.ViewState>() {
            @Override
            public void onChanged(@Nullable StopViewModel.ViewState viewState) {
                if (viewState.state == StopViewModel.ViewState.State.LOADING) {
                    progress.setVisibility(View.VISIBLE);
                    swipeRefresh.setVisibility(View.INVISIBLE);
                    swipeRefresh.setEnabled(false);
                } else if (viewState.state == StopViewModel.ViewState.State.CONTENT) {
                    progress.setVisibility(View.INVISIBLE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    swipeRefresh.setEnabled(true);
                    swipeRefresh.setRefreshing(false);

                    getSupportActionBar().setTitle(viewState.content.getName());
                    getSupportActionBar().setSubtitle(viewState.content.getCode());
                    departureAdapter.submitList(viewState.content.getStoptimesWithoutPatterns());
                } else if (viewState.state == StopViewModel.ViewState.State.REFRESHING) {
                    progress.setVisibility(View.INVISIBLE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    //swipeRefresh.setEnabled(false);
                    swipeRefresh.setRefreshing(true);

                    if (departureAdapter.getItemCount() == 0) {
                        departureAdapter.submitList(viewState.content.getStoptimesWithoutPatterns());
                    }
                }
            }
        });
    }
}
