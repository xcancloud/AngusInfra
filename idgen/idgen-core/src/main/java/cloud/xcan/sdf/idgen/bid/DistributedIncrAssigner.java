package cloud.xcan.sdf.idgen.bid;

public interface DistributedIncrAssigner {

  Long incr(String generatorKey, long i);

}
