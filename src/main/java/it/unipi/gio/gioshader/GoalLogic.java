package it.unipi.gio.gioshader;

import it.unipi.gio.gioshader.model.Goal;
import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;
@Component
public class GoalLogic implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GoalLogic.class);

    private ShutterShelly shelly;

    private CopyOnWriteArrayList<Goal> goalList;

    public GoalLogic(CopyOnWriteArrayList<Goal> goalList, ShutterShelly shelly){
        this.goalList = goalList;
        this.shelly = shelly;
        //autostart
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(true){
            if (!goalList.isEmpty()) {
                for (Goal g : goalList) {
                    if (g.inTimeInterval()) {
                       checkHeight(g);
                       checkTilt(g);
                    }
                }
            }
            try {
                Thread.sleep(3000);
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
}
