package fi.hsl.digitransit;

import java.io.IOException;

import fi.hsl.digitransit.domain.Stop;
import fi.hsl.digitransit.domain.StopAtDistanceConnection;

public interface DigitransitAPI {
    StopAtDistanceConnection queryStopsByLocation(double latitude, double longitude, int radius) throws Exception;
    Stop[] queryStops() throws Exception;
    Stop[] queryStops(String name) throws Exception;
}
