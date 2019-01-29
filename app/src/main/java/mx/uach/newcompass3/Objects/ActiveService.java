package mx.uach.newcompass3.Objects;

/**
 * Created by Alt on 09/12/2018.
 */

public class ActiveService {
    private int service;
    private int attending;
    private int roadSupportIndex;
    private double originlat;
    private double originlon;
    private double destinationlat;
    private double destinationlon;
    private float distance;
    private String key;
    private String client;
    private String date;
    private String rTime;
    private String fOrder;

    public ActiveService() {
    }

    public ActiveService(int service, int attending, double originlat, double originlon, double destinationlat, double destinationlon, String client, String date, String rTime) {
        this.service = service;
        this.attending = attending;
        this.originlat = originlat;
        this.originlon = originlon;
        this.destinationlat = destinationlat;
        this.destinationlon = destinationlon;
        this.client = client;
        this.date = date;
        this.rTime = rTime;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public int getAttending() {
        return attending;
    }

    public void setAttending(int attending) {
        this.attending = attending;
    }

    public int getRoadSupportIndex() { return roadSupportIndex; }

    public void setRoadSupportIndex(int roadSupportIndex) { this.roadSupportIndex = roadSupportIndex; }

    public double getOriginlat() {
        return originlat;
    }

    public void setOriginlat(double originlat) {
        this.originlat = originlat;
    }

    public double getOriginlon() {
        return originlon;
    }

    public void setOriginlon(double originlon) {
        this.originlon = originlon;
    }

    public double getDestinationlat() {
        return destinationlat;
    }

    public void setDestinationlat(double destinationlat) {
        this.destinationlat = destinationlat;
    }

    public double getDestinationlon() {
        return destinationlon;
    }

    public void setDestinationlon(double destinationlon) {
        this.destinationlon = destinationlon;
    }

    public float getDistance() {return distance;}

    public void setDistance(float distance) { this.distance = distance; }

    public String getKey() {return key;}

    public void setKey(String key) { this.key = key; }

    public String getClient() { return client; }

    public void setClient(String client) { this.client = client; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getrTime() { return rTime; }

    public void setrTime(String rTime) { this.rTime = rTime; }

    public String getfOrder() {
        return fOrder;
    }

    public void setfOrder(String fOrder) {
        this.fOrder = fOrder;
    }
}
