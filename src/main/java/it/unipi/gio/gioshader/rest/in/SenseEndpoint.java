package it.unipi.gio.gioshader.rest.in;

import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/sense")
public class SenseEndpoint {

    private ShutterShelly shelly;

    @Autowired
    public SenseEndpoint(ShutterShelly shelly){
        this.shelly=shelly;
    }

    @RequestMapping(value="/height",method = RequestMethod.GET)
    public ResponseEntity getHeight() {
        Integer height = shelly.getHeight();
        if(height==null){
            return  ResponseEntity.noContent().build();
        }
        HashMap<String, Integer> res = new HashMap<>();
        res.put("heigth",height);
        return ResponseEntity.ok().body(res);
    }

    @RequestMapping(value="/tilt",method = RequestMethod.GET)
    public ResponseEntity getTilt() {
        ShutterShelly.LightLevel level = shelly.getTilt();
        if(level== ShutterShelly.LightLevel.UNDEFINED){
            return  ResponseEntity.noContent().build();
        }
        HashMap<String, String> res = new HashMap<>();
        res.put("tilt_level",level.name());
        return ResponseEntity.ok().body(res);
    }
}
