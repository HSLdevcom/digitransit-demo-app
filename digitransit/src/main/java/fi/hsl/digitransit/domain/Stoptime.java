package fi.hsl.digitransit.domain;

import java.util.Objects;

public class Stoptime {
    //Seconds since midnight (add this to serviceDay to get Unix timestamp)
    private long scheduledArrival;
    //Seconds since midnight
    private long scheduledDeparture;
    //Unix time stamp in seconds
    private long serviceDay;
    private Trip trip;
    private String headsign;

    public Stoptime(long scheduledArrival, long scheduledDeparture, long serviceDay, Trip trip, String headsign) {
        this.scheduledArrival = scheduledArrival;
        this.scheduledDeparture = scheduledDeparture;
        this.serviceDay = serviceDay;
        this.trip = trip;
        this.headsign = headsign;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public long getScheduledArrival() {
        return scheduledArrival;
    }

    public void setScheduledArrival(long scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public long getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(long scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public long getServiceDay() {
        return serviceDay;
    }

    public void setServiceDay(long serviceDay) {
        this.serviceDay = serviceDay;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stoptime stoptime = (Stoptime) o;
        return scheduledArrival == stoptime.scheduledArrival &&
                scheduledDeparture == stoptime.scheduledDeparture &&
                serviceDay == stoptime.serviceDay &&
                Objects.equals(trip, stoptime.trip) &&
                Objects.equals(headsign, stoptime.headsign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledArrival, scheduledDeparture, serviceDay, trip, headsign);
    }
}
