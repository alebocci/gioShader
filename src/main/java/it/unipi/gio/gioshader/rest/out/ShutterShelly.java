package it.unipi.gio.gioshader.rest.out;

import it.unipi.gio.gioshader.model.ShellyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;

public class ShutterShelly {

    private static final Logger LOG = LoggerFactory.getLogger(ShutterShelly.class);

    private InetAddress ip;
    private String baseAddress;
    private RestTemplate restTemplate;
    private int height;
    private LightLevel level;
    private boolean lastDirectionUP;
    private boolean statusValid;
    private Thread worker;
    private boolean opened;
    private boolean closed;

    public enum LightLevel {
        DARK, LOW, MEDIUM, BRIGHT, UNDEFINED
    }

    public ShutterShelly(InetAddress ip){
        this.ip = ip;
        baseAddress = "http://"+ip.getHostName()+"/roller/0?";
        this.restTemplate = new RestTemplate();
        height = 0;
        level = LightLevel.UNDEFINED;
        lastDirectionUP = false;
        statusValid = false;
        opened = false;
        closed = false;
    }

    public synchronized boolean isWorking(){
        ShellyResponse response=null;
        try {
            response = restTemplate.getForObject(baseAddress, ShellyResponse.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return false;
        }

        return response != null && !response.getState().equals("stop");
    }

    private synchronized void shellySync(){
        ShellyResponse response =null;
        try {
            response = restTemplate.getForObject(baseAddress, ShellyResponse.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return;
        }
        if(response==null || !response.getState().equals("stop")){
            level=LightLevel.UNDEFINED;
            statusValid = false;
        }else{
            int current_pos = response.getCurrent_pos();
            this.lastDirectionUP = response.getLast_direction().equals("open");
            if(lastDirectionUP){
                 level=LightLevel.DARK;
             }else{
                 level=LightLevel.BRIGHT;
             }
            this.height=current_pos;

            statusValid=true;
        }
    }

    private synchronized void startWork(){
        worker=Thread.currentThread();
    }

    private synchronized void stopWork(){
        if(worker!=null && worker!=Thread.currentThread()){
            worker.interrupt();
        }
        worker=null;
    }

    private void logStatus(){
        if(!statusValid){
            LOG.info("Status not valid.");
        }else {
            LOG.info("Height: {}, TiltLevel: {}, LastDirectionUP: {}",height,level.name(),lastDirectionUP);
        }
    }

    public synchronized boolean open() {
        shellySync();
        LOG.info("Open request, after synch:");
        logStatus();
        if(!statusValid){return false;}
        if(height==100 || opened){return true;}
        startWork();
        restTemplate.getForObject(baseAddress+"go=open", ShellyResponse.class);
        stopWork();
        opened=true;
        closed=false;
        return true;
    }

    public synchronized boolean close() {
        shellySync();
        LOG.info("Close request, after synch:");
        logStatus();
        if(!statusValid){return false;}
        if(closed){return true;}
        new Thread(() -> {
            restTemplate.getForObject(baseAddress+"go=close", ShellyResponse.class);
            startWork();
            float time = (float)height/10*4; //mediamente ci mette 4 secondi per fare 10 percento di movimento
            long sleep = (long)time*1000;
            boolean moving = true;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {return;}
            ShellyResponse response = null;
            while (moving){
                response = restTemplate.getForObject(baseAddress, ShellyResponse.class);
                if(response!=null && response.getState()!=null) {
                    moving = !(response.getState().equals("stop"));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) { return;}
            }
            restTemplate.getForObject(baseAddress+"go=to_pos&roller_pos=3", ShellyResponse.class);
            stopWork();
        }).start();
        closed=true;
        opened=false;
        return true;
    }

    public synchronized boolean tilt(LightLevel l) {
        shellySync();
        LOG.info("Tilt request {}, after synch:",l.name());
        logStatus();
        if(!statusValid) return false;
        if (l==level && height <5) return true;
        new Thread(() -> {
            restTemplate.getForObject(baseAddress+"go=close", ShellyResponse.class);
            startWork();
            float time = (float)height/10*4; //mediamente ci mette 4 secondi per fare 10 percento di movimento
            long sleep = (long)time*1000;
            boolean moving = true;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {return;}
            ShellyResponse response = null;
            while (moving){
                response = restTemplate.getForObject(baseAddress, ShellyResponse.class);
                if(response!=null && response.getState()!=null) {
                    moving = !(response.getState().equals("stop"));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {return;}
            }
            int pos =0;
            switch (l){
                case BRIGHT:
                    shellySync();
                    this.level=l;
                    return;
                case MEDIUM:
                    pos=1;
                    break;
                case LOW:
                    pos=2;
                    break;
                case DARK:
                    pos=3;
                    break;
            }
            restTemplate.getForObject(baseAddress+"go=to_pos&roller_pos="+pos, ShellyResponse.class);
            stopWork();
            this.level=l;
        }).start();
        opened=false;
        closed=false;
        return true;
    }

    public void tiltThere(boolean open){
        shellySync();
        LOG.info("Tilt there request open={}, after synch:",open);
        logStatus();
        if(open && level==LightLevel.BRIGHT){return;}
        if(!open && level==LightLevel.DARK){return;}
        if(statusValid) {
            startWork();
            if(open){
                height= Math.max(0,height-3);
                level = LightLevel.BRIGHT;
            }
            else {
                height= Math.min(100, height+3);
                level = LightLevel.DARK;
            }
            restTemplate.getForObject(baseAddress+"go=to_pos&roller_pos="+height, ShellyResponse.class);
            stopWork();
        }
        opened=false;
        closed=false;
    }

    public synchronized boolean goTo(int h) {
       if(h<0 || h > 100){return false;}
       shellySync();
        LOG.info("GoTo request h= {}, after sync:",h);
        logStatus();
        if(!statusValid){return false;}
        if((height==h && level==LightLevel.DARK)) {return true;}
        new Thread(() -> {
            int pos;
            boolean toDark;
            if(height>h){//discesa
                pos = Math.max(h-3,0);
                toDark=true;
            }else {//salita
                pos = h;
                toDark=false;
            }
            restTemplate.getForObject(baseAddress+"go=to_pos&roller_pos="+pos, ShellyResponse.class);
            if (!toDark){
                level=LightLevel.DARK;
                return;
            }
            startWork();
            pos = Math.abs(height-pos);
            float time = (float)pos/10*4; //mediamente ci mette 4 secondi per fare 10 percento di movimento
            long sleep = (long)time*1000;
            boolean moving = true;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {return;}
            ShellyResponse response = null;
            while (moving){
                response = restTemplate.getForObject(baseAddress, ShellyResponse.class);
                if(response!=null && response.getState()!=null) {
                    moving = !(response.getState().equals("stop"));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {return;}
            }
            this.shellySync();
            height+=3;
            restTemplate.getForObject(baseAddress+"go=to_pos&roller_pos="+(this.height), ShellyResponse.class);
            stopWork();
        }).start();
        opened=false;
        closed=false;
        return true;
    }

    public void stop(){
        LOG.info("Stop request");
        logStatus();
        restTemplate.getForObject(baseAddress+"go=stop", ShellyResponse.class);
        stopWork();
        opened=false;
        closed=false;
    }

    public synchronized InetAddress getIp() {
        return ip;
    }

    public synchronized void setIp(InetAddress ip) {
        if(!this.ip.equals(ip)) {
            baseAddress = "http://"+ip.getHostAddress()+":80/roller/0?";
            this.ip = ip;
        }
    }

    public synchronized Integer getHeight() {
        shellySync();
        if(!statusValid){
            return null;
        }
        return this.height;
    }

    public synchronized LightLevel getTilt(){
        shellySync();
        if(!statusValid){
            return LightLevel.UNDEFINED;
        }
        return level;
    }

}
