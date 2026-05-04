package cloud.xcan.angus.idgen;

public interface DistributedIncrAssigner {

  Long incr(String generatorKey, long i);

}
