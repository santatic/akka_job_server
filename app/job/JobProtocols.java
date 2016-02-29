package job;

import akka.actor.ActorRef;
import scala.Serializable;

import java.util.LinkedList;

/**
 * Created by sonnam on 1/21/16.
 */
public class JobProtocols {
    public static class JobPackage implements Serializable {
        private LinkedList<ActorRef> refs = new LinkedList<>();
        private Object format;
        public final Object job;
        public final Boolean reply;

        public JobPackage(Object format, Object job, Boolean reply) {
            this.format = format;
            this.job = job;
            this.reply = reply;
        }

        public JobPackage(LinkedList<ActorRef> refs, Object format, Object job, Boolean reply) {
            this.refs = refs;
            this.format = format;
            this.job = job;
            this.reply = reply;
        }

        public void addRef(ActorRef ref) {
            this.refs.addLast(ref);
        }

        public ActorRef pollRef() {
//            if (this.refs.size() > 0) {
            return this.refs.pollLast();
//            }
//            return null;
        }

        public Integer getRefSize() {
            return refs.size();
        }

        public void setFormat(Object format) {
            this.format = format;
        }

        public Object getFormat() {
            return this.format;
        }

        public JobPackage copyWithJob(Object job) {
            return new JobPackage(this.refs, this.format, job, this.reply);
        }

        @Override
        public String toString() {
            return "JobPackage { refs=" + refs + ", format=" + format + ", job=" + job + " }";
        }

    }

    public static class JobProtocol implements Serializable {
    }

    /**
     * Job detail
     */
    public static class JobPushLocationDriverProtocol extends JobProtocol {
        public final String driverId;
        public final Float lat;
        public final Float lon;

        public JobPushLocationDriverProtocol(String driverId, Float lat, Float lon) {
            this.driverId = driverId;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return "JobSearchDriversLocationProtocol { driverId=" + driverId + ", lat=" + lat + ", lon=" + lon + " }";
        }

    }

    public static class JobSearchDriversLocationProtocol extends JobProtocol {
        public final Float lat;
        public final Float lon;

        public JobSearchDriversLocationProtocol(Float lat, Float lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return "JobSearchDriversLocationProtocol { lat=" + lat + ", lon=" + lon + " }";
        }

    }

    public static class JobResultDriversLocationProtocol extends JobProtocol {
        public final String result;

        public JobResultDriversLocationProtocol(String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "JobResultDriversLocationProtocol { result='" + result + "' }";
        }
    }
}
