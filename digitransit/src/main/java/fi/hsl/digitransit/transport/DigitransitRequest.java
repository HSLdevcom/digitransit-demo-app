package fi.hsl.digitransit.transport;

import java.util.Map;
import java.util.Objects;

public class DigitransitRequest {
    private String query;
    private Map<String, ?> variables;

    public DigitransitRequest(String query, Map<String, ?> variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, ?> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, ?> variables) {
        this.variables = variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitransitRequest that = (DigitransitRequest) o;
        return Objects.equals(query, that.query) &&
                Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {

        return Objects.hash(query, variables);
    }
}
