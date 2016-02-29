package master;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.client.ClusterClientReceptionist;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import frontend.FrontendBridger.*;
import job.JobProtocols.*;
import scala.concurrent.duration.FiniteDuration;
import worker.MasterWorkerProtocols.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import master.WorkerStates.*;

/**
 * Created by sonnam on 1/19/16.
 */
public class Master extends UntypedActor {
    private final FiniteDuration jobTimeout;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Cancellable cleanupTask;

//    private final FiniteDuration notifyTimeout;
//    private Deadline notifyDeadline;
    private Boolean notifyLock = false;

    private HashMap<String, WorkerState> workers = new HashMap<String, WorkerState>();
    private ArrayList<JobPackage> jobs = new ArrayList<>();

    public static Props props(FiniteDuration jobTimeout) {
        return Props.create(Master.class, jobTimeout);
    }

    public Master(FiniteDuration jobTimeout) {
        this.jobTimeout = jobTimeout;
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
        this.cleanupTask = getContext().system().scheduler().schedule(jobTimeout.div(2), jobTimeout.div(2), getSelf(), CleanupTick, getContext().dispatcher(), getSelf());

//        notifyTimeout = Duration.create(1, "seconds");
//        notifyDeadline = notifyTimeout.fromNow();
    }

    @Override
    public void postStop() {
        cleanupTask.cancel();
    }

    private void notifyWorkers() {
//        log.info("=====>>> notifyWorkers Got: {}", workers);
//        if (notifyDeadline.isOverdue() && workers.size() > 0) {
        if (!notifyLock && workers.size() > 0) {
//            notifyDeadline = notifyTimeout.fromNow();
            notifyLock = true;
            for (String workerId : workers.keySet()) {
                WorkerState state = workers.get(workerId);
                if (state.status.isIdle() || (!state.status.isIdle() && state.status.getDeadLine().isOverdue())){
//                    log.info("=====>>> notifyWorkers available: {}", state);
                    state.ref.tell(WorkerIsJobReadyProtocol.getInstance(), getSelf());
                    workers.put(workerId, state.copyWithStatus(new Busy(jobTimeout.fromNow())));
                }
            }
            notifyLock = false;
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
//        log.info("=====>>> Master Got: {}", message);

        if (message instanceof JobPackage) {
            JobPackage pkg = (JobPackage) message;
            if (pkg.getFormat() instanceof FrontendSendMasterProtocol){
                pkg.setFormat(MasterSendJobProtocol.instance);
                if (pkg.reply){
                    pkg.addRef(getSender());
                }
                jobs.add(0, pkg);
                notifyWorkers();
            }
        } else if (message instanceof WorkerRegistryMasterProtocol) {
            String workerId = ((WorkerRegistryMasterProtocol) message).workerId;
            if (workers.containsKey(workerId)) {
                workers.put(workerId, workers.get(workerId).copyWithRef(getSender()));
            } else {
                log.info("=====>>> Worker registered: {}", workerId);
                workers.put(workerId, new WorkerState(getSender(), Idle.instance));
                if (jobs.size() > 0) {
                    getSender().tell(WorkerIsJobReadyProtocol.getInstance(), getSelf());
                }
            }
        } else if (message instanceof WorkerRequestJobProtocol) {
            WorkerRequestJobProtocol content = (WorkerRequestJobProtocol) message;
            final WorkerState state = workers.get(content.workerId);

            if(jobs.size() > 0){
                workers.put(content.workerId, state.copyWithStatus(new Busy(jobTimeout.fromNow())));
                Integer count = Math.min(jobs.size(), content.jobCount);
                List<JobPackage> list = jobs.subList(jobs.size() - count, jobs.size());
                ArrayList<JobPackage> arrayList = new ArrayList<>(list);
                list.clear();

                MasterSendJobsProtocol data = new MasterSendJobsProtocol(arrayList);
//                log.info("\n\n\n\n\n=====>>> WorkerRequestJobProtocol list: {}\ndata: {}\njobs: {}\n\n\n", list, data, jobs);
                getSender().tell(data, ActorRef.noSender());
            }else{
                // set worker idle, update sender
                workers.put(content.workerId, new WorkerState(getSender(), Idle.instance));
            }
        } else {
            unhandled(message);
        }
    }

    /*********************/
    public static final Object CleanupTick = new Object() {
        @Override
        public String toString() {
            return "CleanupTick";
        }
    };
}
