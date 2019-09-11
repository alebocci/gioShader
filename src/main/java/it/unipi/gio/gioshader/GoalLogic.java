package it.unipi.gio.gioshader;

import it.unipi.gio.gioshader.model.Goal;
import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GoalLogic implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GoalLogic.class);

    private ShutterShelly shelly;

    private Goal goal;

    private AtomicBoolean goalActive;
    private String urlToPing;

    private RestTemplate restTemplate;

    public GoalLogic(ShutterShelly shelly, RestTemplate restTemplate){
        this.shelly = shelly;
        goalActive = new AtomicBoolean(true);
        this.restTemplate=restTemplate;
        //autostart
        new Thread(this).start();
    }

    @Override
    public void run() {
        int secondSleep;
        int pings=0;
        while(true){
            if(goalActive.get()) {
                Goal g = getGoal();
                if (g != null) {
                    if (g.inTimeInterval()) {
                        checkHeight(g);
                        checkTilt(g);
                    }
                }
                secondSleep = 120;
            }else{
                if(!checkConnectionAlive()){
                    if(++pings==3) {
                        activateGoals();
                    }else {
                        pings = 0;
                    }
                }
                secondSleep = 60;
            }
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    private void checkHeight(Goal g){
        int bottom = g.getHeightBottom();
        int top = g.getHeightTop();
        if (top==-1){top=101;}
        Integer height = shelly.getHeight();
        if(height==null){return;}
        if(!(bottom <= height && height<= top)){
            shelly.goTo((top+bottom)/2);
        }
    }

    private void checkTilt(Goal g){
        if(g.getLevel()== ShutterShelly.LightLevel.UNDEFINED){
            return;
        }
        ShutterShelly.LightLevel level = g.getLevel();
        if(level==shelly.getTilt()) return;
        if(level== ShutterShelly.LightLevel.DARK){
            shelly.tiltThere(false);
        }else if( level== ShutterShelly.LightLevel.BRIGHT){
            shelly.tiltThere(true);
        }else{
            shelly.tilt(level);
        }
    }

    public synchronized Goal getGoal() {
        return goal;
    }

    public synchronized boolean setGoal(Goal goal) {
        if(goalActive.get()) {
            this.goal = goal;
            return true;
        }
        return false;
    }

    public synchronized void activateGoals() {
        goalActive.set(true);
        this.urlToPing=null;
    }

    public synchronized boolean disactivateGoals(String urlToPing) {
        if(this.urlToPing!=null){
            return false;
        }
        goalActive.set(false);
        this.urlToPing=urlToPing;
        return true;
    }

    private boolean checkConnectionAlive(){
        LOG.info("Ping service above");
        ResponseEntity<Void> response;
        try {
            response =  restTemplate.getForEntity(urlToPing+"/goals/ping", Void.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return false;
        }
        return response.getStatusCode().is2xxSuccessful();
    }

}
