package specman.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/** Multi-flavor clipboard transferable for Specman steps. Offers the Specman-native flavor
 * (carrying instance ID + serialized content as a structured object) and plain text as fallback
 * for interoperability with external tools. */
public class SpecmanTransferable implements Transferable {

  public static final DataFlavor SPECMAN_STEPS_FLAVOR =
      new DataFlavor(SpecmanClipboardContent.class, "Specman Steps");

  private static final DataFlavor[] SUPPORTED_FLAVORS = {SPECMAN_STEPS_FLAVOR, DataFlavor.stringFlavor};

  private final SpecmanClipboardContent content;

  public SpecmanTransferable(SpecmanClipboardContent content) {
    this.content = content;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return SUPPORTED_FLAVORS.clone();
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return SPECMAN_STEPS_FLAVOR.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    if (SPECMAN_STEPS_FLAVOR.equals(flavor)) {
      return content;
    }
    if (DataFlavor.stringFlavor.equals(flavor)) {
      return content.serializedSteps;
    }
    throw new UnsupportedFlavorException(flavor);
  }
}
