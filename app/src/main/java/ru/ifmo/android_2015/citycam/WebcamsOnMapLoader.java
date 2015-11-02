package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.webcams.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by dmitry.trunin on 01.11.2015.
 */
public class WebcamsOnMapLoader extends JsonHttpLoader<List<Webcam>> {

    private final LatLng southWestCorner;
    private final LatLng northEastCorner;
    private final double zoomLevel;

    private static final String ARG_SW_CORNER = "sw";
    private static final String ARG_NE_CORNER = "ne";
    private static final String ARG_ZOOM_LEVEL = "zoom";

    public static Bundle createArgs(LatLng southWestCorner,
                                    LatLng northEastCorner,
                                    double zoomLevel) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_SW_CORNER, southWestCorner);
        args.putParcelable(ARG_NE_CORNER, northEastCorner);
        args.putDouble(ARG_ZOOM_LEVEL, zoomLevel);
        return args;
    }

    public WebcamsOnMapLoader(Context context,
                              Bundle args) {
        super(context);
        this.southWestCorner = args.getParcelable(ARG_SW_CORNER);
        this.northEastCorner = args.getParcelable(ARG_NE_CORNER);
        this.zoomLevel = args.getDouble(ARG_ZOOM_LEVEL);
    }

    @Override
    protected URL createURL() throws MalformedURLException {
        return Webcams.createMapBboxUrl(southWestCorner.latitude, southWestCorner.longitude,
                northEastCorner.latitude, northEastCorner.longitude, zoomLevel);
    }

    @Override
    protected List<Webcam> parseResponse(JsonReader reader) throws IOException {
        List<Webcam> webcams = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name == null) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "webcams":
                    parseWebcams(reader, webcams);
                    break;

                case "status":
                    String status = reader.nextString();
                    if (!"ok".equals(status)) {
                        throw new IOException("Webcam API response: " + status);
                    }
                    break;

                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return webcams;
    }

    private void parseWebcams(JsonReader reader, List<Webcam> outWebcams) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("webcam".equals(name)) {
                parseWebcamArray(reader, outWebcams);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private void parseWebcamArray(JsonReader reader, List<Webcam> outWebcams) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            Webcam webcam = parseWebcam(reader);
            if (webcam != null) {
                outWebcams.add(webcam);
            }
        }
        reader.endArray();
    }

    private Webcam parseWebcam(JsonReader reader) throws IOException {
        String title = null;
        String latitude = null;
        String longitude = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("title".equals(name)) {
                title = reader.nextString();
            } else if ("latitude".equals(name)) {
                latitude = reader.nextString();
            } else if ("longitude".equals(name)) {
                longitude = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        if (title == null || latitude == null || longitude == null) {
            return null;
        }

        try {
            return new Webcam(
                    Double.parseDouble(latitude),
                    Double.parseDouble(longitude),
                    title);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Error parsing webcam: " + e, e);
        }
        return null;
    }
}
