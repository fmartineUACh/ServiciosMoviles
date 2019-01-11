package mx.uach.newcompass3.Objects;

/**
 * Created by Alt on 09/12/2018.
 */

public class ActiveService {
    int service;
    int attending;
    double originlat;
    double originlon;
    double destinitylat;
    double destinitylon;
    float distance;
    String distUnit;

    public ActiveService() {
    }

    public ActiveService(int service, int attending, double originlat, double originlon, double destinitylat, double destinitylon) {
        this.service = service;
        this.attending = attending;
        this.originlat = originlat;
        this.originlon = originlon;
        this.destinitylat = destinitylat;
        this.destinitylon = destinitylon;
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

    public float getDistance() {return distance;}

    public void setDistance(float distance) { this.distance = distance; }

    public String getDistUnit() {return distUnit;}

    public void setDistUnit(String distUnit) { this.distUnit = distUnit; }
}
