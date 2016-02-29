package master;

import akka.actor.ActorRef;
import scala.concurrent.duration.Deadline;

/**
 * Created by sonnam on 1/21/16.
 */
public class WorkerStates {
    public static abstract class WorkerStatus {
        protected abstract boolean isIdle();

        private boolean isBusy() {
            return !isIdle();
        }

        protected abstract String getJobId();

        protected abstract Deadline getDeadLine();
    }

    public static final class Idle extends WorkerStatus {
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

    public static final class Busy extends WorkerStatus {
//        private final String jobId;
        private final Deadline deadline;

//        private Busy(String jobId, Deadline deadline) {
        protected Busy(Deadline deadline) {
//            this.jobId = jobId;
            this.deadline = deadline;
        }

        @Override
        protected boolean isIdle() {
            return false;
        }

        @Override
//        protected String getJobId() {
//            return jobId;
//        }
        protected String getJobId() {
            return null;
        }

        @Override
        protected Deadline getDeadLine() {
            return deadline;
        }

        @Override
//        public String toString() {
//            return "Busy{" + "jobId=" + jobId + ", deadline=" + deadline + '}';
//        }
        public String toString() {
            return "Busy{ deadline=" + deadline + " }";
        }
    }

    public static final class WorkerState {
        public final ActorRef ref;
        public final WorkerStatus status;

        public WorkerState(ActorRef ref, WorkerStatus status) {
            this.ref = ref;
            this.status = status;
        }

        public WorkerState copyWithRef(ActorRef ref) {
            return new WorkerState(ref, this.status);
        }

        protected WorkerState copyWithStatus(WorkerStatus status) {
            return new WorkerState(this.ref, status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || !getClass().equals(o.getClass()))
                return false;

            WorkerState that = (WorkerState) o;

            if (!ref.equals(that.ref))
                return false;
            if (!status.equals(that.status))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = ref.hashCode();
            result = 31 * result + status.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "WorkerState{" + "ref=" + ref + ", status=" + status + '}';
        }
    }
}
