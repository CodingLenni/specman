package specman.clipboard;

import specman.model.v002.TextEditAreaModel_V002;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/** Multi-flavor clipboard transferable for Specman text content. Adds the Specman-native
 * flavor on top of the original JEditorPane transferable, delegating all other flavors
 * (HTML, RTF, plain text, etc.) to it unchanged for full external-app compatibility. */
public class SpecmanTextTransferable implements Transferable {

  public static final DataFlavor SPECMAN_TEXT_FLAVOR =
      new DataFlavor(TextEditAreaModel_V002.class, "Specman Text");

  private final TextEditAreaModel_V002 content;
  private final Transferable original;

  public SpecmanTextTransferable(TextEditAreaModel_V002 content, Transferable original) {
    this.content = content;
    this.original = original;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    DataFlavor[] originalFlavors = original != null ? original.getTransferDataFlavors() : new DataFlavor[0];
    DataFlavor[] all = new DataFlavor[1 + originalFlavors.length];
    all[0] = SPECMAN_TEXT_FLAVOR;
    System.arraycopy(originalFlavors, 0, all, 1, originalFlavors.length);
    return all;
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return SPECMAN_TEXT_FLAVOR.equals(flavor)
        || (original != null && original.isDataFlavorSupported(flavor));
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    if (SPECMAN_TEXT_FLAVOR.equals(flavor)) {
      return content;
    }
    if (original != null) {
      try {
        return original.getTransferData(flavor);
      }
      catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    throw new UnsupportedFlavorException(flavor);
  }
}
