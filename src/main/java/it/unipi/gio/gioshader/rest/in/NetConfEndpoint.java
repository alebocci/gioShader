package it.unipi.gio.gioshader.rest.in;

import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conf")
public class NetConfEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(NetConfEndpoint.class);

    private ShutterShelly shelly;

    @Autowired
    public NetConfEndpoint(ShutterShelly shelly){
        this.shelly = shelly;
    }



    @RequestMapping(value="/ip",method = RequestMethod.PUT)
    public ResponseEntity setIp(@RequestBody BodyIp body) {
        LOG.info("New ip request, ip: {}",body.ip);
        shelly.setIp(body.ip);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/ip",method = RequestMethod.GET)
    public ResponseEntity geIp() {
        LOG.info("Get ip request");
        BodyIp res = new BodyIp();
        res.setIp(shelly.getIp());
        return ResponseEntity.ok(res);
    }


    public static class BodyIp{
        String ip;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }
}
