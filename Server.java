// Representation of Moon Tracker: https://i.imgur.com/rOmDb5l.png
import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import com.sun.net.httpserver.*;

public class Server {
    private static final String QUERY_TEMPLATE = "{\"location\":[\"%s\"]}";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Create an HttpServer instance on port 8001 accepting up to 100 concurrent connections
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 100);
        // Return the index.html file when the browser asks for the web app
        server.createContext("/", (HttpExchange t) -> {
            String html = Files.readString(Paths.get("index.html"));
            send(t, "text/html; charset=utf-8", html);
        });
        server.createContext("/query", (HttpExchange t) -> {
            //Stores the user's input for the target height as a double.
            double targetHeight = Double.parseDouble(parse("TargetHeight", t.getRequestURI().getQuery().split("&")));

            //Stores the user's input for the target longitude as a double.
            double targetLong = Double.parseDouble(parse("TargetLong", t.getRequestURI().getQuery().split("&")));

            //Stores the user's input for the target latitude as a double.
            double targetLat = Double.parseDouble(parse("TargetLat", t.getRequestURI().getQuery().split("&")));

            //Stores the user's input for date and time as a string, ignoring the time zone.
            String localdtString = parse("DateTime", t.getRequestURI().getQuery().split("&"));

            //Stores the user's input for the time zones as a string.
            String timeZone = parse("Zone", t.getRequestURI().getQuery().split("&"));

            LocalDateTime localdt = LocalDateTime.parse(localdtString,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            //Caclulates the hours to be offseted from UTC
            int offsetHours=6;
            switch(timeZone){
                case "PST" : offsetHours = -8;
                    break;
                case "MST" : offsetHours = -7;
                    break;
                case "CST" : offsetHours = -6;
                    break;
                case "EST" : offsetHours = -5;
                    break;
            }
            ZonedDateTime zdt = ZonedDateTime.ofInstant(localdt, ZoneOffset.ofHours(offsetHours), ZoneId.of(timeZone, ZoneId.SHORT_IDS));
            String location = calculation(targetLat, targetLong, targetHeight,zdt);
            send(t, "application/json", String.format(QUERY_TEMPLATE, location));
            return;         
        });
        server.setExecutor(null);
        server.start();
    }

    //It takes a latitude and longitude of a target point as well as the zoned date and time of choice.
    //It calculates and returns a String that represents the (latitude,longitude) of the location where the
    //user should stand to get their photo of the moon and target. If the moon has not risen yet at that 
    //location and time, then there is no location to stand, so it returns "The moon hasn't risen yet."
    private static String calculation(double targetLat, double targetLong, double height, ZonedDateTime zdt){
        Coordinate target = new Coordinate(targetLat, targetLong, height);
        Moon moon = new Moon(targetLat, targetLong, zdt);
        double[] moonPos = moon.AzAlt();
        Coordinate moonCoord = new Coordinate(Math.toRadians(moonPos[0] + Math.PI), Math.toRadians(moonPos[1]));

        try{
            Line line = new Line(moonCoord, target);
            line.zIntersection(-height);
        }catch(Exception e){
            return "The moon hasn't risen yet.";
        }
        double direction = moonPos[0];// + 180;  // degrees West of South + 180 = degrees East of North
        double distance = height / Math.tan(Math.toRadians(moonPos[1]));
        double NSchange = distance * Math.sin(Math.toRadians(90 - direction));
        double EWchange = distance * Math.cos(Math.toRadians(90 - direction));
        double[] latLong = distToLatLong(targetLat, targetLong, NSchange, EWchange);
        return "(" + latLong[0] + ", " + latLong[1] + ")";
    }

    // Takes in an original point in latitude and longitude, and distances (in meters) in East-West and North-South directions.
    // Positive EW is in the East direction, and positive NS is in the North direction.
    // Returns a double[] containing latitude and longitude of a new point, which is the given distances away from the original.
    private static double[] distToLatLong(double lat1, double long1, double NS, double EW) {
        double R = 6378137;  // Earth spherical radius

        // Coordinate offsets in radians
        double dLat = NS / R;
        double dLong = EW / (R * Math.cos(Math.toRadians(lat1)));

        // New coordinates in degrees
        double newLat = lat1 + Math.toDegrees(dLat);
        double newLong = long1 + Math.toDegrees(dLong);

        return new double[] {newLat, newLong};
    }

    private static String parse(String key, String... params) {
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }
        return "";
    }

    private static void send(HttpExchange t, String contentType, String data)
            throws IOException, UnsupportedEncodingException {
        t.getResponseHeaders().set("Content-Type", contentType);
        byte[] response = data.getBytes("UTF-8");
        t.sendResponseHeaders(200, response.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response);
        }
    }

    private static String json(Iterable<String> matches) {
        StringBuilder results = new StringBuilder();
        for (String s : matches) {
            if (results.length() > 0) {
                results.append(',');
            }
            // Step 1: Return the top 5 matches as a JSON object (dict)
            results.append('"').append(s).append('"');
        }
        return results.toString();
    }

    
}
