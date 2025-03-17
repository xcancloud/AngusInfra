package cloud.xcan.sdf.validator;

public enum EditorType {
  QUILL, MARKDOWN;

  public boolean isQuill() {
    return this == EditorType.QUILL;
  }
}
