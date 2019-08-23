package it.unipi.gio.gioshader.model;


public class ShellyResponse {
    private String state;
    private Integer power;
    private Boolean is_valid;
    private Boolean safety_switch;
    private String stop_reason;
    private String last_direction;
    private boolean calibrating;
    private int current_pos;
    private boolean positioning;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public Boolean getIs_valid() {
        return is_valid;
    }

    public void setIs_valid(Boolean is_valid) {
        this.is_valid = is_valid;
    }

    public Boolean getSafety_switch() {
        return safety_switch;
    }

    public void setSafety_switch(Boolean safety_switch) {
        this.safety_switch = safety_switch;
    }

    public String getStop_reason() {
        return stop_reason;
    }

    public void setStop_reason(String stop_reason) {
        this.stop_reason = stop_reason;
    }

    public String getLast_direction() {
        return last_direction;
    }

    public void setLast_direction(String last_direction) {
        this.last_direction = last_direction;
    }

    public boolean isCalibrating() {
        return calibrating;
    }

    public void setCalibrating(boolean calibrating) {
        this.calibrating = calibrating;
    }

    public int getCurrent_pos() {
        return current_pos;
    }

    public void setCurrent_pos(int current_pos) {
        this.current_pos = current_pos;
    }

    public boolean isPositioning() {
        return positioning;
    }

    public void setPositioning(boolean positioning) {
        this.positioning = positioning;
    }
}
