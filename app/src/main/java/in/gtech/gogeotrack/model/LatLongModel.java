package in.gtech.gogeotrack.model;

/**
 * Created by silence12 on 10/7/17.
 */

public class LatLongModel {

    public int id;
    public String name;
    public String uniqueId;
    public String status;
    public String lastUpdates;
    public String category;
    public int positionId;
    public String timezone;
    public Double latitute;
    public Double longitute;
    public String address;
    public Double speed;
    public Double distance_travelled;
    public String time;
    public String timeDiff;

    public LatLongModel() {

    }

    public LatLongModel(int id, String name, String status, Double latitute, Double longitute) {
        this.id = id;
        this.status = status;
        this.latitute = latitute;
        this.longitute = longitute;
        this.name = name;

    }


    public LatLongModel(int id, String name, String uniqueId, String status, String lastUpdates, String category, int positionId, String address, String time, String timeDiff) {
        this.id = id;
        this.name = name;
        this.uniqueId = uniqueId;
        this.status = status;
        this.lastUpdates = lastUpdates;
        this.category = category;
        this.positionId = positionId;
        this.address = address;
        this.time = time;
        this.timeDiff = timeDiff;


    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastUpdates() {
        return lastUpdates;
    }

    public void setLastUpdates(String lastUpdates) {
        this.lastUpdates = lastUpdates;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


    public Double getLatitute() {
        return latitute;
    }

    public void setLatitute(Double latitute) {
        this.latitute = latitute;
    }

    public Double getLongitute() {
        return longitute;
    }

    public void setLongitute(Double longitute) {
        this.longitute = longitute;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getDistance_travelled() {
        return distance_travelled;
    }

    public void setDistance_travelled(Double distance_travelled) {
        this.distance_travelled = distance_travelled;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeDiff() {
        return timeDiff;
    }

    public void setTimeDiff(String timeDiff) {
        this.timeDiff = timeDiff;
    }
}
