package in.gtech.gogeotrack.activity.Reports;

/**
 * Created by surya on 12/10/17.
 */

public class ReportData {
    public String type;
    public String time;
    public String deviceName;
    public Double speed;

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public int valid;

    public String address;
    public Double latitude;

    public Double distance;
    public Double averageSpeed;
    public Double maximumSpeed;

    public String startTime;
    public String endTime;
    public int duration;
    public String startAddress;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getSpentFuel() {
        return spentFuel;
    }

    public void setSpentFuel(String spentFuel)
    {
        this.spentFuel = spentFuel;
    }

    public String endAddress;
    public String spentFuel;


    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public Double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(Double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public int getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(int engineHours) {
        this.engineHours = engineHours;
    }

    public int engineHours;


    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double longitude;
    public Double altitude;



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getGeofence() {
        return geofence;
    }

    public void setGeofence(int geofence) {
        this.geofence = geofence;
    }

    public int geofence;
    public String geo;

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public ReportData(String deviceName, String type, String time, int geofence){
        this.deviceName = deviceName;
        this.type = type;
        this.time = time;
        this.geofence = geofence;
    }
    public ReportData(String deviceName,String type,String time,String geofence){
        this.deviceName = deviceName;
        this.type = type;
        this.time = time;
        this.geo = geofence;
    }

    public ReportData(String deviceName,int valid,String time,Double speed,String address,Double latitude,Double longitude,Double altitude){
        this.deviceName = deviceName;
        this.valid = valid;
        this.time = time;
        this.speed = speed;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>for Summary Report>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..
    public ReportData(String deviceName,Double distance,Double averageSpeed,Double maximumSpeed,int engineHours){
        this.deviceName = deviceName;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
        this.maximumSpeed = maximumSpeed;
        this.engineHours = engineHours;
    }
    public ReportData(String deviceName,String startTime,String endTime,int duration,String startAddress,String endAddress,String spentFuel,Double distance,Double averageSpeed,Double maximumSpeed){
        this.deviceName = deviceName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.spentFuel = spentFuel;

        this.averageSpeed = averageSpeed;
        this.maximumSpeed = maximumSpeed;
        this.distance = distance;
    }

}
