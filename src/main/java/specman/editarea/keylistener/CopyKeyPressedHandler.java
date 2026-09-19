package specman.editarea.keylistener;

import specman.clipboard.SpecmanTextTransferable;
import specman.editarea.TextEditArea;
import specman.model.v002.TextEditAreaModel_V002;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

class CopyKeyPressedHandler extends AbstractKeyEventHandler {

  CopyKeyPressedHandler(TextEditArea textArea, KeyEvent event) {
    super(textArea, event);
  }

  void handle() {
    String selected = textArea.getSelectedText();
    if (selected == null || selected.isEmpty()) {
      return;
    }
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    textArea.copy();
    Transferable original = clipboard.getContents(null);
    TextEditAreaModel_V002 content = new TextEditAreaModel_V002(
        selected, selected, new ArrayList<>(), (specman.ChangeInfo) null);
    clipboard.setContents(new SpecmanTextTransferable(content, original), null);
    event.consume();
  }
}
