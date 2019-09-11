package it.unipi.gio.gioshader.model;

import it.unipi.gio.gioshader.rest.out.ShutterShelly;

import java.time.LocalTime;

public class Goal {

    private int startHour=-1;
    private int stopHour=-1;
    private int heightBottom = -1;
    private int heightTop = -1;
    private ShutterShelly.LightLevel level = ShutterShelly.LightLevel.UNDEFINED;

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        if(startHour<0 || startHour>23) {startHour=-1;}
        this.startHour = startHour;
    }

    public int getStopHour() {
        return stopHour;
    }

    public void setStopHour(int stopHour) {
        if(stopHour<0 || stopHour>23) {stopHour=-1;}
        this.stopHour = stopHour;
    }


    public int getHeightBottom() {
        return heightBottom;
    }

    public void setHeightBottom(int heightBottom) {
        this.heightBottom = heightBottom;
    }

    public int getHeightTop() {
        return heightTop;
    }

    public void setHeightTop(int heightTop) {
        this.heightTop = heightTop;
    }

    public ShutterShelly.LightLevel getLevel() {
        return level;
    }

    public void setLevel(ShutterShelly.LightLevel level) {
        this.level = level;
    }

    private boolean hourSet(){return startHour!=-1 || stopHour!=-1;}

    public boolean inTimeInterval(){
        if (!hourSet()){return true;}
        int tempStop = stopHour;
        int hourNow = LocalTime.now().getHour();
        if(startHour==-1){return hourNow<stopHour;}
        if(stopHour==-1){return hourNow>startHour;}
        if(startHour > stopHour){
            tempStop +=24;
            hourNow +=24;
        }
        return (startHour <= hourNow && hourNow < tempStop);
    }
}
