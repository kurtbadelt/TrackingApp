package tech.e32.lendme_rastreo;

/**
 * Created by kurtbadelt on 4/2/17.
 */

public class UbicacionConductor {


    private String email;
    private String Conductor;
    private String Placas;
    private String timeStamp;
    private String terminosYCondiciones;
    private String Latitude;
    private String Longitud;

    public void crearConductor(String email, String Conductor, String Placas, String timeStamp, String terminosYCondiciones, String Latitude, String Longitude){
        this.setEmail(email);
        this.setConductor(Conductor);
        this.setPlacas(Placas);
        this.setTimeStamp(timeStamp);
        this.setTerminosYCondiciones(terminosYCondiciones);
        this.setLatitude(Latitude);
        this.setLongitud(Longitude);

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getConductor() {
        return Conductor;
    }

    public void setConductor(String conductor) {
        Conductor = conductor;
    }

    public String getPlacas() {
        return Placas;
    }

    public void setPlacas(String placas) {
        Placas = placas;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTerminosYCondiciones() {
        return terminosYCondiciones;
    }

    public void setTerminosYCondiciones(String terminosYCondiciones) {
        this.terminosYCondiciones = terminosYCondiciones;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitud() {
        return Longitud;
    }

    public void setLongitud(String longitud) {
        Longitud = longitud;
    }




}
