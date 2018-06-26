package fi.hsl.digitransit.domain;

import java.util.Objects;

public class StopAtDistanceEdge {
    private StopAtDistance node;

    public StopAtDistanceEdge(StopAtDistance node) {
        this.node = node;
    }

    public StopAtDistance getNode() {
        return node;
    }

    public void setNode(StopAtDistance node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopAtDistanceEdge that = (StopAtDistanceEdge) o;
        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }
}
