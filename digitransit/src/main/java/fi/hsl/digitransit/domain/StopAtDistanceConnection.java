package fi.hsl.digitransit.domain;

import java.util.List;
import java.util.Objects;

public class StopAtDistanceConnection {
    private List<StopAtDistanceEdge> edges;

    public StopAtDistanceConnection(List<StopAtDistanceEdge> edges) {
        this.edges = edges;
    }

    public List<StopAtDistanceEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<StopAtDistanceEdge> edges) {
        this.edges = edges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopAtDistanceConnection that = (StopAtDistanceConnection) o;
        return Objects.equals(edges, that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edges);
    }
}
