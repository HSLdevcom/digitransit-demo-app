package fi.hsl.digitransit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.StandardProtocolFamily;
import java.util.List;

import fi.hsl.digitransit.domain.DigitransitResponse;
import fi.hsl.digitransit.domain.Stop;
import fi.hsl.digitransit.domain.StopAtDistanceConnection;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DigitransitAPI {
    private static final String API_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql";

    private Gson gson = new GsonBuilder()
            //register type adapters for gson
            .registerTypeAdapter(new TypeToken<DigitransitResponse<StopAtDistanceConnection>>(){}.getType(), new DigitransitResponse.DigitransitResponseDeserializer<>(StopAtDistanceConnection.class))
            .registerTypeAdapter(new TypeToken<DigitransitResponse<Stop[]>>(){}.getType(), new DigitransitResponse.DigitransitResponseDeserializer<>(Stop[].class))
            .create();

    private OkHttpClient httpClient;

    public DigitransitAPI(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public StopAtDistanceConnection queryStopsByLocation(double latitude, double longitude, int radius) throws IOException {
        //Query stops (and 5 next departures from each stop) by location
        String query = "{\n" +
                "  stopsByRadius(lat:" + latitude + ", lon:" + longitude + ", radius:" + radius + ") {\n" +
                "    edges {\n" +
                "      node {\n" +
                "        stop { \n" +
                "          gtfsId \n" +
                "          name\n" +
                "          code\n" +
                "          lat\n" +
                "          lon\n" +
                "          stoptimesWithoutPatterns(numberOfDepartures: 5) {\n" +
                "            scheduledArrival\n" +
                "            scheduledDeparture\n" +
                "            serviceDay\n" +
                "            trip { tripHeadsign route { shortName } }\n" +
                "          }\n" +
                "        }\n" +
                "        distance\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        return doQuery(query, StopAtDistanceConnection.class);
    }

    public Stop[] queryStops() throws IOException {
        //List all stops with name and GTFS id
        String query = "{ \n" +
                        "  stops {\n" +
                        "    gtfsId\n" +
                        "    name\n" +
                        "  }\n" +
                        "}";

        return doQuery(query, Stop[].class);
    }

    private <T> T doQuery(String query, Class<T> klass) throws IOException {
        //HTTP POST request, content type: "application/graphql"
        Request request = new Request.Builder().url(API_URL).post(RequestBody.create(MediaType.parse("application/graphql"), query)).build();

        try (Response response = httpClient.newCall(request).execute()) {
            //Parse response using Gson
            //Response will be parsed to a DigitransitResponse object
            return ((DigitransitResponse<T>)gson.fromJson(response.body().charStream(), DigitransitResponse.createType(klass))).getValue();
        }
    }
}