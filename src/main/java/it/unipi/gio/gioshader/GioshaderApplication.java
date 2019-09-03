package it.unipi.gio.gioshader;

import it.unipi.gio.gioshader.model.Goal;
import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class GioshaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GioshaderApplication.class, args);
	}

	@Bean
	public ExitCodeGenerator exitCodeGenerator() {
		return () -> 42;
	}

	@Bean
	public CopyOnWriteArrayList<Goal> goalList(){return new CopyOnWriteArrayList<>();}

	@Bean
	public ShutterShelly getShelly(Environment env){
		String address = env.getProperty("shelly.address");
		ShutterShelly sh=null;
		try {
			 sh = new ShutterShelly(InetAddress.getByName(address));
		} catch (UnknownHostException e) {
			System.out.println("Shelly fail");
			System.exit(-2);
		}
		return sh;
	}
}
