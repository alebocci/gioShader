package it.unipi.gio.gioshader.rest.in;


import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

@RestController
@RequestMapping("/api/act")
public class ActEndpoint {


    private static final Logger LOG = LoggerFactory.getLogger(ActEndpoint.class);

    private ShutterShelly shelly;

    @Autowired
    public ActEndpoint(ShutterShelly shelly){
        this.shelly=shelly;
    }

    @RequestMapping(value="/close",method = RequestMethod.PUT)
    public ResponseEntity close() {
        if(shelly.isWorking()){
            LOG.info("Close request refused: Shelly Locked");
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
       if(!shelly.close()){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();}
       return ResponseEntity.accepted().build();
    }

    @RequestMapping(value="/open",method = RequestMethod.PUT)
    public ResponseEntity open() {
        if(shelly.isWorking()){
            LOG.info("Open request refused: Shelly Locked");
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        if(!shelly.open()){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();}
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value="/goto",method = RequestMethod.PUT)
    public ResponseEntity goTo(@RequestParam(name="pos")  int pos) {
        if(pos <0 || pos >100){
            return ResponseEntity.badRequest().body("Invalid position value.");
        }
        if(shelly.isWorking()){
            LOG.info("Go to request refused: Shelly Locked");
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        if(!shelly.goTo(pos)){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();}
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value="/tilt",method = RequestMethod.PUT)
    public ResponseEntity tilt(@RequestParam(name="pos", required = false) Integer pos,
                               @RequestParam(name="name", required = false) String l) {
        if((pos==null && l==null) || (pos!=null && l!=null)){
            return ResponseEntity.badRequest().body("Either \"name\" or \"pos\" parameters are required.");
        }
        if(shelly.isWorking()){
            LOG.info("Tilt request refused: Shelly Locked");
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        ShutterShelly.LightLevel level;
        if (pos != null) {
            switch (pos) {
                case 1:
                    level = ShutterShelly.LightLevel.DARK;
                    break;
                case 2:
                    level = ShutterShelly.LightLevel.LOW;
                    break;
                case 3:
                    level = ShutterShelly.LightLevel.MEDIUM;
                    break;
                case 4:
                    level = ShutterShelly.LightLevel.BRIGHT;
                    break;
                case 0:
                    shelly.tiltThere(false);
                    return ResponseEntity.accepted().build();
                case 5:
                    shelly.tiltThere(true);
                    return ResponseEntity.accepted().build();
                default:
                    return ResponseEntity.badRequest().body("Invalid position value.");
            }
        }else {
            switch (l){
                case "dark":
                    level= ShutterShelly.LightLevel.DARK;
                    break;
                case "low":
                    level= ShutterShelly.LightLevel.LOW;
                    break;
                case "medium":
                    level= ShutterShelly.LightLevel.MEDIUM;
                    break;
                case "bright":
                    level= ShutterShelly.LightLevel.BRIGHT;
                    break;
                case "open":
                    shelly.tiltThere(true);
                    return ResponseEntity.accepted().build();
                case "close":
                    shelly.tiltThere(false);
                    return ResponseEntity.accepted().build();
                default:
                    return ResponseEntity.badRequest().body("Invalid position name.");
            }
        }
        if(!shelly.tilt(level)){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();}
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value="/stop",method = RequestMethod.PUT)
    public ResponseEntity stop() {
        shelly.stop();
        return ResponseEntity.accepted().build();
    }
}
