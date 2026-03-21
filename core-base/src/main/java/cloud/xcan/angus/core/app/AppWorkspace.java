package cloud.xcan.angus.core.app;

import cloud.xcan.angus.core.utils.SpringAppDirUtils;

public class AppWorkspace {

  SpringAppDirUtils appDirUtils = new SpringAppDirUtils();

  public String getHomeDir() {
    return appDirUtils.getHomeDir();
  }

  public String getLibDir() {
    return appDirUtils.getLibDir();
  }

  public String getLibDir(String homeDir) {
    return appDirUtils.getLibDir(homeDir);
  }

  public String getTmpDir() {
    return appDirUtils.getTmpDir();
  }

  public String getTmpDir(String homeDir) {
    return appDirUtils.getTmpDir(homeDir);
  }

  public String getBizTmpDir(String bizDir) {
    return appDirUtils.getBizTmpDir(bizDir);
  }

  public String getBizWorkDir(String homeDir, String bizDir) {
    return appDirUtils.getBizWorkDir(homeDir, bizDir);
  }

  public String getWorkDir() {
    return appDirUtils.getWorkDir();
  }

  public String getWorkDir(String homeDir) {
    return appDirUtils.getWorkDir(homeDir);
  }

  public String getBizWorkDir(String bizDir) {
    return appDirUtils.getBizWorkDir(bizDir);
  }

  public String getWorkDir(String homeDir, String bizDir) {
    return appDirUtils.getWorkDir(homeDir, bizDir);
  }

  public String getDataDir() {
    return appDirUtils.getDataDir();
  }

  public String getDataDir(String homeDir) {
    return appDirUtils.getDataDir(homeDir);
  }

  public String getBizDataDir(String bizDir) {
    return appDirUtils.getBizDataDir(bizDir);
  }

  public String getBizDataDir(String homeDir, String bizDir) {
    return appDirUtils.getBizDataDir(homeDir, bizDir);
  }

  public String getLogsDir() {
    return appDirUtils.getLogsDir();
  }

  public String getLogsDir(String homeDir) {
    return appDirUtils.getLogsDir(homeDir);
  }

  public String getBizLogsDir(String bizDir) {
    return appDirUtils.getBizLogsDir(bizDir);
  }

  public String getBizLogsDir(String homeDir, String bizDir) {
    return appDirUtils.getBizLogsDir(homeDir, bizDir);
  }

  public String getConfDir() {
    return appDirUtils.getConfDir();
  }

  public String getConfDir(String homeDir) {
    return appDirUtils.getConfDir(homeDir);
  }

  public String getPluginDir() {
    return appDirUtils.getPluginDir();
  }

  public String getPluginDir(String homeDir) {
    return appDirUtils.getPluginDir(homeDir);
  }

  public String getSubAppDir() {
    return appDirUtils.getSubAppDir();
  }

  public String getSubAppDir(String homeDir) {
    return appDirUtils.getSubAppDir(homeDir);
  }

  public String getSubAppDirPlugin(String subAppName) {
    return appDirUtils.getSubAppDirPlugin(subAppName);
  }

  public String getSubAppDirPlugin(String homeDir, String subAppName) {
    return appDirUtils.getSubAppDirPlugin(homeDir, subAppName);
  }

  public String getStaticDir() {
    return appDirUtils.getStaticDir();
  }

  public String getStaticDir(String homeDir) {
    return appDirUtils.getStaticDir(homeDir);
  }

  public String getBizStaticDir(String bizDir) {
    return appDirUtils.getStaticDir() + bizDir;
  }

  public String getStaticPluginDir() {
    return appDirUtils.getStaticPluginDir();
  }

  public String getStaticPluginDir(String homeDir) {
    return appDirUtils.getStaticPluginDir(homeDir);
  }

  public String getBizConfDir(String bizDir) {
    return appDirUtils.getBizConfDir(bizDir);
  }

  public String getBizConfDir(String homeDir, String bizDir) {
    return appDirUtils.getBizConfDir(homeDir, bizDir);
  }
}
