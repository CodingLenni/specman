package specman.editarea.keylistener;

import specman.editarea.TextEditArea;

import java.awt.event.KeyEvent;

import static specman.Specman.editor;

class CutKeyPressedHandler extends CopyKeyPressedHandler {

  CutKeyPressedHandler(TextEditArea textArea, KeyEvent event) {
    super(textArea, event);
  }

  @Override
  void handle() {
    super.handle();
    if (isTrackingChanges()) {
      markSelectedTextAsDeletedInModificationMode();
    }
    else {
      textArea.replaceSelection("");
    }
  }
}
