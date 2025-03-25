package cloud.xcan.angus.validator;

public enum EditorType {
  QUILL, MARKDOWN;

  public boolean isQuill() {
    return this == EditorType.QUILL;
  }
}
