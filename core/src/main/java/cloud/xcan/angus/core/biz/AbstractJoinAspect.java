package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.remote.ApiConstant.LCS_PUB;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.app.AppWorkspace;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.remote.PageResult;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.typelevel.v.Str0;

public abstract class AbstractJoinAspect {

  protected Object aspect(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();
    if (Objects.isNull(result)) {
      return null;
    }
    if (result.getClass().isArray()) {
      Object[] voArray = (Object[]) result;
      if (voArray.length == 0) {
        return result;
      }
      joinArrayVoName(voArray);
    } else if (result instanceof Collection) {
      Collection<?> voCollection = (Collection<?>) result;
      if (voCollection.isEmpty()) {
        return result;
      }
      joinArrayVoName(voCollection.toArray());
    } else if (result instanceof PageResult) {
      PageResult<?> pageResult = (PageResult<?>) result;
      List<?> voList = pageResult.getList();
      if (isEmpty(voList)) {
        return result;
      }
      joinArrayVoName(voList.toArray());
    } else {
      joinArrayVoName(Lists.newArrayList(result).toArray());
    }
    return result;
  }

  protected abstract void joinArrayVoName(Object[] toArray) throws IllegalAccessException;

  public static String writePublicFileWhenNotExists() {
    String fileName =
        new AppWorkspace().getWorkDir() /*+ LCS_DIR // -> confusing paths */ + LCS_PUB;
    File publicFile = new File(fileName);
    try {
      // Fix: Prevent ineffective upgrades and replacements
      if (publicFile.exists()) {
        publicFile.delete();
      }

      InputStream stream = null;
      try {
        // Warning: Development and prod environments use the same configuration
        stream = SpringContextHolder.class.getResourceAsStream("/" + new Str0(
            new long[]{0x1FD7C425A441EEA3L, 0x607B2B538143A9DDL, 0x856DD71B65B34080L,
                0xD47F89A37D540550L, 0x9C78C085BDE0AE08L, 0x4FB8C83CD88E7F52L})
            .toString()) /* => "cert/XCanTest.publicCert.keystore" */;
        FileUtils.copyInputStreamToFile(stream, publicFile);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (null != stream) {
          IOUtils.closeQuietly(stream);
        }
      }
    } catch (Exception e) {
      System.out.println(
          new cloud.xcan.angus.api.obf.Str0(
              new long[]{0x8B60B722C3C6EF28L, 0xD4DEF5A0D5325EEBL, 0x2E744FB20DEFC5A9L,
                  0xDDECDCB81E0B9377L, 0xEDD4A420BF7A38A9L})
              .toString() /* => "Create public lcs key failed" */);
      return null;
    }
    return fileName;
  }

}
