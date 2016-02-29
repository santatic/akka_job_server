package worker;

import akka.actor.*;
import akka.cluster.client.ClusterClient;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import frontend.FrontendBridger.*;
import job.JobProtocols.*;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;

import worker.MasterWorkerProtocols.*;
import worker.WorkerJobProtocols.*;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;

/**
 * Created by sonnam on 1/20/16.
 */
public class Worker extends UntypedActor {
    private final String workerId = UUID.randomUUID().toString();
    private final Cancellable registerTask;
    private final ActorRef jobExecutor;
    private final ActorRef clusterClient;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    //    private String jobId = null;
    private final Integer jobLimit = 200;
    private Integer jobRunning = 0;
    //    private Integer jobRequesting = 0;

    public static Props props(ActorRef clusterClient, Props jobExecutorProps, FiniteDuration registerInterval) {
        return Props.create(Worker.class, clusterClient, jobExecutorProps, registerInterval);
    }

    public static Props props(ActorRef clusterClient, Props workExecutorProps) {
        return props(clusterClient, workExecutorProps, Duration.create(10, "seconds"));
    }

    public Worker(ActorRef clusterClient, Props jobExecutorProps, FiniteDuration registerInterval) {
        this.clusterClient = clusterClient;
        this.jobExecutor = getContext().watch(getContext().actorOf(jobExecutorProps, "exec"));
        this.registerTask = getContext().system().scheduler().schedule(Duration.Zero(), registerInterval,
                clusterClient, new ClusterClient.SendToAll("/user/master/singleton", new WorkerRegistryMasterProtocol(workerId)),
                getContext().dispatcher(), getSelf());
    }

    @Override
    public void postStop() {
        registerTask.cancel();
    }

    @Override
    public void onReceive(Object message) throws Exception {
//        log.info("=====>>> Worker got {}", message);
        if (message instanceof WorkerIsJobReadyProtocol) {
            log.info("===== >>> {} : jobLimit={}, jobRunning={}", workerId, jobLimit, jobRunning);
            if (jobLimit - jobRunning > 0) {
                sendToMaster(new WorkerRequestJobProtocol(workerId, jobLimit - jobRunning));
            }
        } else if (message instanceof MasterSendJobsProtocol) {
            MasterSendJobsProtocol content = (MasterSendJobsProtocol) message;
            jobRunning += content.jobs.size();
            for (JobPackage job : content.jobs) {
                job.setFormat(new WorkerSendJobProtocol());
                jobExecutor.tell(job, getSelf());
            }
            if (jobLimit - jobRunning > 0) {
                sendToMaster(new WorkerRequestJobProtocol(workerId, jobLimit - jobRunning));
            }
        } else if (message instanceof JobPackage) {
            JobPackage pkg = (JobPackage) message;
            if (pkg.getFormat() instanceof JobResultDoneProtocol) {
                if (pkg.reply && pkg.getRefSize() > 0){
                    pkg.setFormat(WorkerResultFrontendProtocol.instance);
                    ActorRef ref = pkg.pollRef();
                    ref.tell(pkg, ActorRef.noSender());
                }
                jobRunning = Math.max(0, jobRunning - 1);
                if (jobLimit > jobRunning) {
                    sendToMaster(new WorkerRequestJobProtocol(workerId, jobLimit - jobRunning));
                }
            }
        } else {
            unhandled(message);
        }
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(-1, Duration.Inf(),
                new Function<Throwable, SupervisorStrategy.Directive>() {
                    @Override
                    public SupervisorStrategy.Directive apply(Throwable t) {
                        log.info("=====>>> OneForOneStrategy {}.", t);
                        if (t instanceof ActorInitializationException)
                            return stop();
                        else if (t instanceof DeathPactException)
                            return stop();
                        else if (t instanceof Exception) {
                            jobRunning = Math.max(0, jobRunning - 1);
                            if (jobLimit - jobRunning > 0) {
                                sendToMaster(new WorkerRequestJobProtocol(workerId, jobLimit - jobRunning));
                            }
//                            if (jobId != null)
//                                sendToMaster(new WorkerJobFailedProtocol(workerId, jobId()));
//                            getContext().become(idle);
                            return restart();
                        } else {
                            return escalate();
                        }
                    }
                }
        );
    }

    @Override
    public void unhandled(Object message) {
        if (message instanceof Terminated && ((Terminated) message).getActor().equals(jobExecutor)) {
            getContext().stop(getSelf());
        } else if (message instanceof WorkerIsJobReadyProtocol) {
            // do nothing
        } else {
            super.unhandled(message);
        }
    }

    private void sendToMaster(Object msg) {
        clusterClient.tell(new ClusterClient.SendToAll("/user/master/singleton", msg), getSelf());
    }

}
