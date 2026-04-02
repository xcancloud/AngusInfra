package cloud.xcan.angus.idgen.bid;

public interface DistributedIncrAssigner {

  Long incr(String generatorKey, long i);

}
