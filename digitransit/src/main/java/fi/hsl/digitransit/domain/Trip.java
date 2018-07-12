package fi.hsl.digitransit.domain;

import java.util.Objects;

public class Trip {
    private String gtfsId;
    private String tripHeadsign;
    private Route route;

    public Trip(String gtfsId, String tripHeadsign, Route route) {
        this.gtfsId = gtfsId;
        this.tripHeadsign = tripHeadsign;
        this.route = route;
    }

    public String getGtfsId() {
        return gtfsId;
    }

    public void setGtfsId(String gtfsId) {
        this.gtfsId = gtfsId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public void setTripHeadsign(String tripHeadsign) {
        this.tripHeadsign = tripHeadsign;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(gtfsId, trip.gtfsId) &&
                Objects.equals(tripHeadsign, trip.tripHeadsign) &&
                Objects.equals(route, trip.route);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gtfsId, tripHeadsign, route);
    }
}
