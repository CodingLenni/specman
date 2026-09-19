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
    textArea.copy();
    Transferable original = clipboard.getContents(null);
    TextEditAreaModel_V002 content = buildSelectionModel();
    clipboard.setContents(new SpecmanTextTransferable(content, original), null);
    event.consume();
  }

  private TextEditAreaModel_V002 buildSelectionModel() {
    return textArea.copySection(
        getWrappedSelectionStart(), getWrappedSelectionEnd()).getTextWithMarkups(true);
  }
}
