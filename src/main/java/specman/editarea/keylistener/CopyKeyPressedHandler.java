package specman.editarea.keylistener;

import specman.clipboard.SpecmanTextTransferable;
import specman.editarea.TextEditArea;
import specman.model.v002.TextEditAreaModel_V002;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

class CopyKeyPressedHandler extends AbstractKeyEventHandler {

  CopyKeyPressedHandler(TextEditArea textArea, KeyEvent event) {
    super(textArea, event);
  }

  void handle() {
    if (textArea.getSelectedText() == null || textArea.getSelectedText().isEmpty()) {
      return;
    }
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable original = createOriginalJEditorPaneTransferable(clipboard);
    TextEditAreaModel_V002 content = buildSelectionModel();
    clipboard.setContents(new SpecmanTextTransferable(content, original), null);
    event.consume();
  }

  /** JEditorPane's copy action produces a multi-flavor transferable (HTML, RTF, plain text etc.)
   * that external apps like Word need for formatted paste. We trigger it to capture that
   * transferable, then wrap it with our Specman flavor on top — external apps delegate
   * transparently, Specman paste gets the Specman flavor. */
  private Transferable createOriginalJEditorPaneTransferable(Clipboard clipboard) {
    textArea.copy();
    return clipboard.getContents(null);
  }

  private TextEditAreaModel_V002 buildSelectionModel() {
    return textArea.copySection(
        getWrappedSelectionStart(), getWrappedSelectionEnd()).getTextWithMarkups(true);
  }
}
