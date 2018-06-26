package fi.hsl.digitransit.domain;

import java.util.Objects;

public class Route {
    private String shortName;

    public Route(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(shortName, route.shortName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(shortName);
    }
}
