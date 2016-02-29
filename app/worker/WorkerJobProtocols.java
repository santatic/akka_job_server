package worker;

import akka.actor.ActorRef;
import scala.Serializable;

import java.util.LinkedList;

/**
 * Created by sonnam on 1/22/16.
 */
public class WorkerJobProtocols {

    public static class WorkerSendJobProtocol {
    }

    public static class JobResultDoneProtocol implements Serializable {
        public static final JobResultDoneProtocol instance = new JobResultDoneProtocol();
    }
}
