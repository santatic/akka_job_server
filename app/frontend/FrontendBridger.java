package frontend;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import job.JobProtocols.*;
import scala.Serializable;


/**
 * Created by sonnam on 1/20/16.
 */
public class FrontendBridger extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    public static Props props = Props.create(FrontendBridger.class);

    private ActorRef master = getContext().actorOf(
            ClusterSingletonProxy.props(
                    "/user/master",
                    ClusterSingletonProxySettings.create(getContext().system()).withRole("backend")),
            "masterProxy");

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("FrontendBridger Got: {}", message);

        if (message instanceof JobPackage) {
            JobPackage pkg = (JobPackage) message;
            if (pkg.getFormat() instanceof WebSendBridgerProtocol) {
                pkg.setFormat(FrontendSendMasterProtocol.instance);
                if (pkg.reply) {
                    pkg.addRef(getSender());
                    master.tell(pkg, getSelf());
                } else {
                    getSender().tell("{success: true}", ActorRef.noSender());
                    master.tell(pkg, ActorRef.noSender());
                }
            } else if (pkg.getFormat() instanceof WorkerResultFrontendProtocol) {
                pkg.pollRef().tell(pkg.job.toString(), ActorRef.noSender());
            }
        } else {
            unhandled(message);
        }
    }

    /***
     * Protocol
     ***/

    public static class WebSendBridgerProtocol {
        public static final WebSendBridgerProtocol instance = new WebSendBridgerProtocol();
    }

    public static class FrontendSendMasterProtocol implements Serializable {
        public static final FrontendSendMasterProtocol instance = new FrontendSendMasterProtocol();
    }

    public static class WorkerResultFrontendProtocol implements Serializable {
        public static final WorkerResultFrontendProtocol instance = new WorkerResultFrontendProtocol();
    }
}
