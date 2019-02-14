package mx.uach.newcompass3.Objects;

public class LocationTracking {
    private String time;
    private double lat;
    private double lng;
    private double speed;

    public LocationTracking() {
    }

    public LocationTracking(String time, double lat, double lng) {
        this.time = time;
        this.lat = lat;
        this.lng = lng;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getlng() {
        return lng;
    }

    public void setlng(double lng) {
        this.lng = lng;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
