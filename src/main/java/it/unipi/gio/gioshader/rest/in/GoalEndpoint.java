package it.unipi.gio.gioshader.rest.in;

import it.unipi.gio.gioshader.GoalLogic;
import it.unipi.gio.gioshader.model.Goal;
import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/*
* Class that manages the endpoint of the goals.
* Operations allowed: add new goal, get goal list, modify goal by id, delete goal by id*/
@RestController
@RequestMapping("/api/goal")
public class GoalEndpoint {


    private static final Logger LOG = LoggerFactory.getLogger(GoalEndpoint.class);

    private GoalLogic logic;

    /*Unique id for every goal*/
    private AtomicInteger id = new AtomicInteger();

    @Autowired
    public GoalEndpoint(GoalLogic logic){
        this.logic=logic;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity getTest() {
        LOG.info("Goal Test Request");
        return ResponseEntity.badRequest().body("" +
                "{\n" +
                "\t\"test\": \"ok\"\n" +
                "}");
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Goal> getGoal() {
        Goal ret = logic.getGoal();
        if(ret==null){return ResponseEntity.notFound().build();}
        return ResponseEntity.ok(ret);
    }

    /**/
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity setNewGoal(@RequestBody Goal g) {
        LOG.info("Set goal request, g: {}",g);
        int bottom = g.getHeightBottom();
        int top = g.getHeightTop();
        if(bottom==-1 && top==-1 && g.getLevel()== ShutterShelly.LightLevel.UNDEFINED){
            return ResponseEntity.badRequest().body("You must set at least one property.");
        }
        if(top==-1) top=101;
        if(top<=bottom){
            return ResponseEntity.badRequest().body("Top height must be greater or equal than bottom height.");
        }
        if(((bottom+top)/2)<4){
            return ResponseEntity.badRequest().body("A goal height less than 4 is not supported.");
        }

        if(g.getLevel()!= ShutterShelly.LightLevel.UNDEFINED && g.getLevel()!= ShutterShelly.LightLevel.DARK && g.getLevel()!= ShutterShelly.LightLevel.BRIGHT && top!=101 && bottom!=-1){
            return ResponseEntity.badRequest().body("A goal with intermediate tilt level is supported only without top and bottom heights.");
        }
        if(!logic.setGoal(g)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(g);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity deleteGoal(){
        logic.setGoal(null);
        return ResponseEntity.ok().build();
    }
    @RequestMapping(value="/disable", method = RequestMethod.PUT)
    public ResponseEntity disableGoals(HttpServletRequest request, @RequestBody Map<String,String> body) {
        if(body==null || !body.containsKey("port")){
            return ResponseEntity.badRequest().body("Port where contact server not found");
        }
        String serverPort = body.get("port");
        LOG.info("Goal disable request at port "+serverPort);

        String url = "http://"+request.getRemoteAddr()+":"+serverPort;
        if(!logic.disactivateGoals(url)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/enable", method = RequestMethod.PUT)
    public ResponseEntity enableGoals() {
        LOG.info("Goal enable request");
        logic.activateGoals();
        return ResponseEntity.ok().build();
    }
}
