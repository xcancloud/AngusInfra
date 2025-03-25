package cloud.xcan.angus.remote;

public interface CommonMessage {

  String APP_NOT_OPENED_CODE = "SDF301";
  String APP_NOT_OPENED_T = "xcm.comm.app.not.opened.t";
  String APP_EXPIRED_CODE = "SDF302";
  String APP_EXPIRED_T = "xcm.comm.app.expired.t";

  String SHARE_PASSD_ERROR_T = "xcm.comm.share.passd.error";
  String SHARE_TOKEN_ERROR_T = "xcm.comm.share.pt.error";

  String AUDIT_STATUS_ERROR = "xcm.comm.audit.status.error";
  String AUDIT_STATUS_ERROR_TYPE = "xcm.comm.audit.status.error.t";
  String AUDIT_FAILED_REASON_REQUIRED = "xcm.comm.audit.failedReason.required";

  String EXPORT_ROW_OVERT_LIMIT_CODE = "SDF303";
  String EXPORT_ROW_OVERT_LIMIT_T = "xcm.export.rows.over.limit.t";
}
