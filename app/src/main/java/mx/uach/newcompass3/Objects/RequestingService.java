package mx.uach.newcompass3.Objects;

public class RequestingService {
    private int service;
    private Boolean attending;
    private double originlat;
    private double originlon;
    private double destinationlat;
    private double destinationlon;
    private String client;
    private String date;
    private String rTime;

    public RequestingService() {
    }

    public RequestingService(int service, Boolean attending, double originlat, double originlon, double destinationlat, double destinationlon, String client, String date, String rTime) {
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

    public Boolean getAttending() {
        return attending;
    }

    public void setAttending(Boolean attending) {
        this.attending = attending;
    }

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

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getrTime() {
        return rTime;
    }

    public void setrTime(String rTime) {
        this.rTime = rTime;
    }
}
