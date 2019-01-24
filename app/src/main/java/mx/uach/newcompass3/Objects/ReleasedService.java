package mx.uach.newcompass3.Objects;

public class ReleasedService {
    private int service;
    private double originlat;
    private double originlon;
    private double destinitylat;
    private double destinitylon;
    private String client;
    private String driver;
    private String date;
    private String rTime;
    private String aTime;

    public ReleasedService() {
    }

    public ReleasedService(int service, double originlat, double originlon, double destinitylat, double destinitylon, String client, String driver, String date, String rTime, String aTime) {
        this.service = service;
        this.originlat = originlat;
        this.originlon = originlon;
        this.destinitylat = destinitylat;
        this.destinitylon = destinitylon;
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

    public double getDestinitylat() {
        return destinitylat;
    }

    public void setDestinitylat(double destinitylat) {
        this.destinitylat = destinitylat;
    }

    public double getDestinitylon() {
        return destinitylon;
    }

    public void setDestinitylon(double destinitylon) {
        this.destinitylon = destinitylon;
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
}
