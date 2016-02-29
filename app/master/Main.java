package master;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Created by sonnam on 1/19/16.
 */
public class Main {
    public static void main(String[] argv) throws InterruptedException {
        if (argv.length == 0) {
            startBackend(2551, "backend");
//            Thread.sleep(5000);
//            startBackend(2552, "backend");
//            Thread.sleep(5000);
//            startBackend(2553, "backend");
//            Thread.sleep(5000);
//            startBackend(2554, "backend");
//            Thread.sleep(5000);
//            startBackend(2555, "backend");

            Thread.sleep(5000);
            for (int i = 0; i < 3; i++) {
                worker.Main.startWorker(0);
            }

        } else {
            int port = Integer.parseInt(argv[0]);
            startBackend(port, "backend");
        }
    }

    private static FiniteDuration workTimeout = Duration.create(10, "seconds");

    public static void startBackend(int port, String role) {
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + role + "]").
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)).
                withFallback(ConfigFactory.load("master"));

        ActorSystem system = ActorSystem.create("ClusterSystem", conf);

        system.actorOf(
                ClusterSingletonManager.props(
                        Master.props(workTimeout),
                        PoisonPill.getInstance(),
                        ClusterSingletonManagerSettings.create(system).withRole(role)
                ),
                "master");
    }
}
