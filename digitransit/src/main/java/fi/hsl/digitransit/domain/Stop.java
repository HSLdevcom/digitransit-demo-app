package fi.hsl.digitransit.domain;

import java.util.List;
import java.util.Objects;

public class Stop {
    private String gtfsId;
    private String name;
    private double lat;
    private double lon;
    private String code;
    private List<Stoptime> stoptimesWithoutPatterns;

    public Stop(String gtfsId, String name, double lat, double lon, String code, List<Stoptime> stoptimesWithoutPatterns) {
        this.gtfsId = gtfsId;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.code = code;
        this.stoptimesWithoutPatterns = stoptimesWithoutPatterns;
    }

    public String getGtfsId() {
        return gtfsId;
    }

    public void setGtfsId(String gtfsId) {
        this.gtfsId = gtfsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Stoptime> getStoptimesWithoutPatterns() {
        return stoptimesWithoutPatterns;
    }

    public void setStoptimesWithoutPatterns(List<Stoptime> stoptimesWithoutPatterns) {
        this.stoptimesWithoutPatterns = stoptimesWithoutPatterns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return Double.compare(stop.lat, lat) == 0 &&
                Double.compare(stop.lon, lon) == 0 &&
                Objects.equals(gtfsId, stop.gtfsId) &&
                Objects.equals(name, stop.name) &&
                Objects.equals(code, stop.code) &&
                Objects.equals(stoptimesWithoutPatterns, stop.stoptimesWithoutPatterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gtfsId, name, lat, lon, code, stoptimesWithoutPatterns);
    }
}
