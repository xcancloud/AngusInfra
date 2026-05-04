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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class AbstractJoinAspect {

  protected Object aspect(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();
    if (isEmpty(result)) {
      return null;
    }
    if (result.getClass().isArray()) {
      Object[] voArray = (Object[]) result;
      if (voArray.length == 0) {
        return result;
      }
      joinArrayVoName(voArray);
    } else if (result instanceof Collection<?> voCollection) {
      if (voCollection.isEmpty()) {
        return result;
      }
      joinArrayVoName(voCollection.toArray());
    } else if (result instanceof PageResult<?> pageResult) {
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

  private static final String PUBLIC_CERT_KEYSTORE_PATH = "cert/XCanTest.publicCert.keystore";
  private static final String CREATE_PUBLIC_KEY_FAILED = "Create public lcs key failed";

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
        stream = SpringContextHolder.class.getResourceAsStream("/" + PUBLIC_CERT_KEYSTORE_PATH);
        FileUtils.copyInputStreamToFile(stream, publicFile);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (null != stream) {
          IOUtils.closeQuietly(stream);
        }
      }
    } catch (Exception e) {
      System.out.println(CREATE_PUBLIC_KEY_FAILED);
      return null;
    }
    return fileName;
  }

}
