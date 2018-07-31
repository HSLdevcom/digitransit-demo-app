package fi.hsl.digitransit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.hsl.digitransit.transport.DigitransitRequest;
import fi.hsl.digitransit.transport.DigitransitResponse;
import fi.hsl.digitransit.domain.Stop;
import fi.hsl.digitransit.domain.StopAtDistanceConnection;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GraphQLDigitransitAPI implements DigitransitAPI {
    //GraphQL API endpoint
    private static final String API_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql";

    //Content-Type used in HTTP POST requests
    private static final MediaType JSON_CONTENT_TYPE = MediaType.parse("application/json");

    private Gson gson = new GsonBuilder()
            //register type adapters for Gson
            .registerTypeAdapter(new TypeToken<DigitransitResponse<StopAtDistanceConnection>>(){}.getType(), new DigitransitResponse.DigitransitResponseDeserializer<>(StopAtDistanceConnection.class))
            .registerTypeAdapter(new TypeToken<DigitransitResponse<Stop[]>>(){}.getType(), new DigitransitResponse.DigitransitResponseDeserializer<>(Stop[].class))
            .registerTypeAdapter(new TypeToken<DigitransitResponse<Stop>>(){}.getType(), new DigitransitResponse.DigitransitResponseDeserializer<>(Stop.class))
            .create();

    private OkHttpClient httpClient;

    public GraphQLDigitransitAPI(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public StopAtDistanceConnection queryStopsByLocation(double latitude, double longitude, int radius) throws IOException {
        //Query stops (and 5 next departures from each stop) by location
        String query = "query StopsByRadius($lat: Float, $lon: Float, $radius: Int) {" +
                "  stopsByRadius(lat: $lat, lon: $lon, radius: $radius) {" +
                "    edges {" +
                "      node {" +
                "        stop {" +
                "          gtfsId" +
                "          name" +
                "          code" +
                "          lat" +
                "          lon" +
                "          stoptimesWithoutPatterns(numberOfDepartures: 5, omitNonPickups: true) {" +
                "            scheduledArrival" +
                "            scheduledDeparture" +
                "            serviceDay" +
                "            headsign" +
                "            trip { gtfsId tripHeadsign route { shortName } }" +
                "          }" +
                "        }" +
                "        distance" +
                "      }" +
                "    }" +
                "  }" +
                "}";

        Map<String, Object> variables = new HashMap<>();
        variables.put("lat", latitude);
        variables.put("lon", longitude);
        variables.put("radius", radius);

        return doQuery(query, variables, StopAtDistanceConnection.class);
    }

    @Override
    public Stop[] queryStops() throws IOException {
        //List all stops with name and GTFS id
        String query = "query Stops {" +
                        "stops {" +
                        "    gtfsId" +
                        "    name" +
                        "  }" +
                        "}";

        return doQuery(query, Stop[].class);
    }

    @Override
    public Stop[] queryStops(String name) throws IOException {
        //Query stops by name
        String query = "query Stops($name: String) {" +
                "  stops(name: $name) {" +
                "    gtfsId" +
                "    name" +
                "  }" +
                "}";

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);

        return doQuery(query, variables, Stop[].class);
    }

    @Override
    public Stop queryStopById(String gtfsId) throws Exception {
        //Query stops by name
        String query = "query StopById($gtfsId: String!) {" +
        "    stop(id: $gtfsId) {" +
        "        gtfsId" +
        "        name" +
        "        code" +
        "        stoptimesWithoutPatterns(numberOfDepartures: 15, timeRange: 7200, omitNonPickups: true) {" +
        "            scheduledArrival" +
        "            scheduledDeparture" +
        "            serviceDay" +
        "            headsign" +
        "            trip { gtfsId tripHeadsign route { shortName } }" +
        "        }" +
        "    }" +
        "}";

        Map<String, Object> variables = new HashMap<>();
        variables.put("gtfsId", gtfsId);

        return doQuery(query, variables, Stop.class);
    }


    private <T> T doQuery(String query, Class<T> klass) throws IOException {
        return doQuery(query, null, klass);
    }

    private <T> T doQuery(String query, Map<String, ?> variables, Class<T> klass) throws IOException {
        //Create HTTP POST body with query and variables
        String postBody = gson.toJson(new DigitransitRequest(query, variables));

        //HTTP POST request, content type: "application/json"
        Request request = new Request.Builder().url(API_URL).post(RequestBody.create(JSON_CONTENT_TYPE, postBody)).build();

        try (Response response = httpClient.newCall(request).execute()) {
            //Parse response using Gson
            //Response will be parsed to a DigitransitResponse object
            return ((DigitransitResponse<T>)gson.fromJson(response.body().charStream(), DigitransitResponse.createType(klass))).getValue();
        }
    }
}