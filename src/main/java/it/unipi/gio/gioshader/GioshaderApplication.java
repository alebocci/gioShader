package it.unipi.gio.gioshader;

import it.unipi.gio.gioshader.model.Goal;
import it.unipi.gio.gioshader.rest.out.ShutterShelly;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class GioshaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GioshaderApplication.class, args);
	}

	@Bean
	public CopyOnWriteArrayList<Goal> goalList(){return new CopyOnWriteArrayList<>();}

	@Bean
	public ShutterShelly getShelly(Environment env){
		String address = env.getProperty("shelly.address");
		Integer port = env.getProperty("shelly.port", Integer.class);
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
