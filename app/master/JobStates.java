package master;

import akka.actor.ActorRef;
import scala.concurrent.duration.Deadline;

import java.util.LinkedList;

/**
 * Created by sonnam on 1/21/16.
 */
public class JobStates {
    public static abstract class JobStatus {
        protected abstract boolean isIdle();

        private boolean isBusy() {
            return !isIdle();
        }

        protected abstract String getJobId();

        protected abstract Deadline getDeadLine();
    }

    public static final class Idle extends JobStatus {
        public static final Idle instance = new Idle();

        public static Idle getInstance() {
            return instance;
        }

        @Override
        protected boolean isIdle() {
            return true;
        }

        @Override
        protected String getJobId() {
            throw new IllegalAccessError();
        }

        @Override
        protected Deadline getDeadLine() {
            throw new IllegalAccessError();
        }

        @Override
        public String toString() {
            return "Idle";
        }
    }

    public static final class Busy extends JobStatus {
        private final String jobId;
        private final Deadline deadline;

        private Busy(String jobId, Deadline deadline) {
            this.jobId = jobId;
            this.deadline = deadline;
        }

        @Override
        protected boolean isIdle() {
            return false;
        }

        @Override
        protected String getJobId() {
            return jobId;
        }

        @Override
        protected Deadline getDeadLine() {
            return deadline;
        }

        @Override
        public String toString() {
            return "Busy{" + "work=" + jobId + ", deadline=" + deadline + '}';
        }
    }

    public static final class JobState {
        public LinkedList<ActorRef> senders;
        public final JobStatus status;

        public JobState(LinkedList<ActorRef> senders, JobStatus status) {
            this.senders = senders;
            this.status = status;
        }

        public JobState copyWithRefs(LinkedList<ActorRef> senders) {
            return new JobState(senders, this.status);
        }

        private JobState copyWithStatus(JobStatus status) {
            return new JobState(this.senders, status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || !getClass().equals(o.getClass()))
                return false;

            JobState that = (JobState) o;

//            if (!ref.equals(that.ref))
//                return false;
            if (!status.equals(that.status))
                return false;

            return true;
        }

//        @Override
//        public int hashCode() {
//            int result = ref.hashCode();
//            result = 31 * result + status.hashCode();
//            return result;
//        }
//
//        @Override
//        public String toString() {
//            return "WorkerState{" + "ref=" + ref + ", status=" + status + '}';
//        }
    }
}
