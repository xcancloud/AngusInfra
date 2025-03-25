package cloud.xcan.angus.core.jpa.dynamic.search;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

public class MergeFiltersAspect {

  @Pointcut("@annotation(cloud.xcan.angus.core.jpa.dynamic.search.MergeQueryFilters)")
  public void queryPointCut() {
  }

  @Before("queryPointCut()")
  public void around(ProceedingJoinPoint joinPoint) throws Throwable {
  }

}

