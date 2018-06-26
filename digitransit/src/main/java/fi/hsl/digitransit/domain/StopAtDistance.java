package fi.hsl.digitransit.domain;

import java.util.Objects;

public class StopAtDistance {
    private Stop stop;
    private int distance;

    public StopAtDistance(Stop stop, int distance) {
        this.stop = stop;
        this.distance = distance;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopAtDistance that = (StopAtDistance) o;
        return distance == that.distance &&
                Objects.equals(stop, that.stop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stop, distance);
    }
}
