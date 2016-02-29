package worker;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import job.JobProtocols.*;
import worker.WorkerJobProtocols.*;

/**
 * Created by sonnam on 1/20/16.
 */
public class JobExecutor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
//        log.info("=====>>> JobExecutor Got {}", message);
        if (message instanceof JobPackage){
            JobPackage pkg = (JobPackage) message;

            if (pkg.getFormat() instanceof WorkerSendJobProtocol){
                if (pkg.job instanceof JobSearchDriversLocationProtocol) {
                    JobSearchDriversLocationProtocol job = (JobSearchDriversLocationProtocol) pkg.job;
                    String result = String.format("%s + %s = %s", job.lat, job.lon, job.lat + job.lon);
//                    log.info("[+] ----->>>>> JobExecutor working {}", result);
                    pkg.setFormat(JobResultDoneProtocol.instance);
                    getSender().tell(pkg.copyWithJob(new JobResultDriversLocationProtocol(result)), ActorRef.noSender());
                }else if (pkg.job instanceof JobPushLocationDriverProtocol){
                    JobPushLocationDriverProtocol job = (JobPushLocationDriverProtocol) pkg.job;
                    pkg.setFormat(JobResultDoneProtocol.instance);
                    log.info("[+] ----->>>>> JobExecutor working {}", job);
                    getSender().tell(pkg.copyWithJob(new JobResultDriversLocationProtocol("")), ActorRef.noSender());
                }
            }

        }
    }
}
