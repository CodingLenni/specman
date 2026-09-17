package specman.clipboard;

/** Payload of the dedicated Specman clipboard flavor. Carries the instance ID of the editor
 * that placed the content, so paste can decide how to handle change-tracking markings. */
public class SpecmanClipboardContent {
  public final String instanceId;
  public final String serializedSteps;

  public SpecmanClipboardContent(String instanceId, String serializedSteps) {
    this.instanceId = instanceId;
    this.serializedSteps = serializedSteps;
  }
}
