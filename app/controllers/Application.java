package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import frontend.FrontendBridger;
import frontend.FrontendBridger.*;
import job.JobProtocols.*;
import play.libs.F;
import play.mvc.*;

import javax.inject.*;

import static akka.pattern.Patterns.ask;

@Singleton
public class Application extends Controller {
    private final ActorRef bridger;

    @Inject
    public Application(ActorSystem system) {
        Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 0)
                .withFallback(ConfigFactory.load("master"));

        ActorSystem clusterSystem = system.create("ClusterSystem", conf);
        this.bridger = clusterSystem.actorOf(FrontendBridger.props, "bridger");
    }

    public Result index() {
        return ok("Your new application is ready.");
    }

    public F.Promise<Result> jobMessage(String message) {
        return sendToFrontend(message, true);
    }

    /**
     * push location drivers
     */
    public F.Promise<Result> pushLocationDriver(String driverId, Float lat, Float lon) {
        return sendToFrontend(new JobPushLocationDriverProtocol(driverId, lat, lon), false);
    }

    /**
     * search location drivers
     */
    public F.Promise<Result> searchDrivers(Float lat, Float lon) {
        return sendToFrontend(new JobSearchDriversLocationProtocol(lat, lon), true);
    }

    /**
     * send object to frontend
     */
    public F.Promise<Result> sendToFrontend(Object content, Boolean reply) {
        return F.Promise.wrap(
                ask(this.bridger, new JobPackage(WebSendBridgerProtocol.instance, content, reply), 30000)
        ).map(response -> ok((String) response));
    }
}
