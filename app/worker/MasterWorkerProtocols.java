package worker;

import job.JobProtocols.*;
import scala.Serializable;

import java.util.ArrayList;

/**
 * Created by sonnam on 1/21/16.
 */
public class MasterWorkerProtocols {
    public static class MasterWorkerProtocol implements Serializable {
    }

    // worker registry master
    public static class WorkerRegistryMasterProtocol extends MasterWorkerProtocol {
        public final String workerId;

        public WorkerRegistryMasterProtocol(String workerId) {
            this.workerId = workerId;
        }

        @Override
        public String toString() {
            return "WorkerRegistryMasterProtocol { workerId='" + workerId + "' }";
        }
    }

    // master ask worker : i have a job for you, are you ready ?
    public static class WorkerIsJobReadyProtocol extends MasterWorkerProtocol {
        private static final WorkerIsJobReadyProtocol instance = new WorkerIsJobReadyProtocol();

        public static WorkerIsJobReadyProtocol getInstance() {
            return instance;
        }
    }

    // worker reply master : i'm ready, send me [jobCount] new job
    public static class WorkerRequestJobProtocol extends MasterWorkerProtocol {
        public final String workerId;
        public final Integer jobCount;

        public WorkerRequestJobProtocol(String workerId, Integer jobCount) {
            this.workerId = workerId;
            this.jobCount = jobCount;
        }

        @Override
        public String toString() {
            return "WorkerRequestJobProtocol { workerId='" + workerId + "' }";
        }
    }

    // master send worker a job: here is your new job
    public static class MasterSendJobProtocol extends MasterWorkerProtocol {
        public static final MasterSendJobProtocol instance = new MasterSendJobProtocol();
    }

    public static class MasterSendJobsProtocol extends MasterWorkerProtocol {
        public final ArrayList<JobPackage> jobs;

        public MasterSendJobsProtocol(ArrayList<JobPackage> jobs) {
            this.jobs = jobs;
        }

        @Override
        public String toString() {
            return "MasterSendJobsProtocol { jobs=" + jobs + " }";
        }
    }

//    // worker run job failure and reply: oh shit, i'm sorry, job is fail
//    public static class WorkerJobFailedProtocol extends MasterWorkerProtocol {
//        public final String workerId;
//        public final String jobId;
//
//        public WorkerJobFailedProtocol(String workerId, String jobId) {
//            this.workerId = workerId;
//            this.jobId = jobId;
//        }
//
//        @Override
//        public String toString() {
//            return "WorkerJobFailed { workerId='" + workerId + "', jobId='" + jobId + "' }";
//        }
//    }

//    // worker run job done and reply: job is done, here is your result
//    public static class WorkerJobDoneProtocol extends MasterWorkerProtocol {
//        public final LinkedList<ActorRef> receivers;
//        public final String workerId;
//        public final String jobId;
//        public final Object result;
//
//        public WorkerJobDoneProtocol(LinkedList<ActorRef> receivers, String workerId, String jobId, Object result) {
//            this.receivers = receivers;
//            this.workerId = workerId;
//            this.jobId = jobId;
//            this.result = result;
//        }
//
//        @Override
//        public String toString() {
//            return "WorkerJobDone { workerId='" + workerId + "', jobId='" + jobId + "', result='" + result + "' }";
//        }
//    }

//    public static class JobReadyProtocol implements Serializable {
//        private static final JobReadyProtocol instance = new JobReadyProtocol();
//        public static JobReadyProtocol getInstance() {
//            return instance;
//        }
//    }
}
