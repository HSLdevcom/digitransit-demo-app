package fi.hsl.demoapp;


import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import fi.hsl.digitransit.domain.Stoptime;

public class DepartureAdapter extends ListAdapter<Stoptime, DepartureAdapter.DepartureViewHolder> {
    private boolean largeViews;

    public DepartureAdapter() {
        this(false);
    }

    public DepartureAdapter(boolean largeViews) {
        super(DIFF_CALLBACK);

        this.largeViews = largeViews;
    }

    @NonNull
    @Override
    public DepartureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DepartureViewHolder(LayoutInflater.from(parent.getContext()).inflate(largeViews ? R.layout.departure_large : R.layout.departure, parent, false));
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