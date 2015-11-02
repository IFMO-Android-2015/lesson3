package ru.ifmo.android_2015.citycam.webcams;

/**
 * Created by dmitry.trunin on 01.11.2015.
 */
public final class Webcam {

    public final double latitude;
    public final double longitude;
    public final String title;

    public Webcam(double latitude, double longitude, String title) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }
}
