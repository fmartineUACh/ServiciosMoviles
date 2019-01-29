package mx.uach.newcompass3.Objects;

public class ReleasedService {
    private int service;
    private double originlat;
    private double originlon;
    private double destinationlat;
    private double destinationlon;
    private String client;
    private String driver;
    private String date;
    private String rTime;
    private String aTime;
    private String fOrder;

    public ReleasedService() {
    }

    public ReleasedService(int service, double originlat, double originlon, double destinationlat, double destinationlon, String client, String driver, String date, String rTime, String aTime) {
        this.service = service;
        this.originlat = originlat;
        this.originlon = originlon;
        this.destinationlat = destinationlat;
        this.destinationlon = destinationlon;
        this.client = client;
        this.driver = driver;
        this.date = date;
        this.rTime = rTime;
        this.aTime = aTime;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
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

    public String getaTime() {
        return aTime;
    }

    public void setaTime(String aTime) {
        this.aTime = aTime;
    }

    public String getfOrder() {
        return fOrder;
    }

    public void setfOrder(String fOrder) {
        this.fOrder = fOrder;
    }
}
