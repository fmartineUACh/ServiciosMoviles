package mx.uach.newcompass3.Objects;

public class RoadSupport {
    private boolean deflatedTire;
    private boolean noGas;
    private boolean leak;
    private boolean brakeFail;
    private boolean battery;

    public RoadSupport() {
    }

    public RoadSupport(boolean deflatedTire, boolean noGas, boolean leak, boolean brakeFail, boolean battery) {
        this.deflatedTire = deflatedTire;
        this.noGas = noGas;
        this.leak = leak;
        this.brakeFail = brakeFail;
        this.battery = battery;
    }

    public boolean getDeflatedTire() {
        return deflatedTire;
    }

    public void setDeflatedTire(boolean deflatedTire) {
        this.deflatedTire = deflatedTire;
    }

    public boolean getNoGas() {
        return noGas;
    }

    public void setNoGas(boolean noGas) {
        this.noGas = noGas;
    }

    public boolean getLeak() {
        return leak;
    }

    public void setLeak(boolean leak) {
        this.leak = leak;
    }

    public boolean getBrakeFail() {
        return brakeFail;
    }

    public void setBrakeFail(boolean brakeFail) {
        this.brakeFail = brakeFail;
    }

    public boolean getBattery() {
        return battery;
    }

    public void setBattery(boolean battery) {
        this.battery = battery;
    }
}
