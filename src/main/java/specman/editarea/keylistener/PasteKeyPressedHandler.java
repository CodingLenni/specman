package specman.editarea.keylistener;

import specman.clipboard.SpecmanTextTransferable;
import specman.editarea.HTMLTags;
import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
import specman.model.v002.Markup_V002;
import specman.model.v002.TextEditAreaModel_V002;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

class PasteKeyPressedHandler extends AbstractKeyEventHandler {
  PasteKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable contents = clipboard.getContents(null);
      if (contents != null) {
        if (contents.isDataFlavorSupported(SpecmanTextTransferable.SPECMAN_TEXT_FLAVOR)) {
          TextEditAreaModel_V002 model = (TextEditAreaModel_V002)
              contents.getTransferData(SpecmanTextTransferable.SPECMAN_TEXT_FLAVOR);
          pasteFormatted(model);
          event.consume();
          return;
        }
        // External content (Word etc.): strip to plain text to avoid messy HTML,        // then let JEditorPane's default paste action insert it.
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          String stringOnly = (String)contents.getTransferData(DataFlavor.stringFlavor);
          contents = new StringSelection(stringOnly);
          clipboard.setContents(contents, null);
        }
        if (contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
          BufferedImage image = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
          textArea.addImage(image);
        }
      }
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  private void pasteFormatted(TextEditAreaModel_V002 model) {
    try {
      textArea.replaceSelection("");
      int caretPos = textArea.getCaretPosition();
      int expectedLen = model.plainText.stripTrailing().length();
      int docLenBefore = textArea.getDocument().getLength();

      HTMLEditorKit kit = (HTMLEditorKit) textArea.getEditorKit();
      HTMLDocument doc = (HTMLDocument) textArea.getDocument();
      kit.insertHTML(doc, caretPos, HTMLTags.bodyContent(model.text), 0, 0, null);

      int actualInserted = textArea.getDocument().getLength() - docLenBefore;
      WrappedDocument wd = getWrappedDocument();
      Action deletePrev = textArea.getActionMap().get(DefaultEditorKit.deletePrevCharAction);

      // Remove leading structural \n: caret right after it, then delete-previous
      if (actualInserted > expectedLen && deletePrev != null) {
        textArea.setCaretPosition(caretPos + 1);
        deletePrev.actionPerformed(new ActionEvent(textArea, 0, ""));
      }
      // Remove trailing structural \n: caret right after it (shifted by leading removal), then delete-previous
      // Caret naturally lands at caretPos + expectedLen = right after pasted content
      if (actualInserted > expectedLen && deletePrev != null) {
        textArea.setCaretPosition(caretPos + actualInserted - 1);
        deletePrev.actionPerformed(new ActionEvent(textArea, 0, ""));
      }
      // TODO: apply Steplink background markups to the inserted range
      // TODO: register pasted Steplinks in the referenced steps
      // TODO: mark pasted range as Added in current changeset if tracking is on
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void collectLeaves(specman.editarea.document.WrappedElement e,
                             List<specman.editarea.document.WrappedElement> leaves) {
    if (e.getElementCount() == 0) leaves.add(e);
    else for (int i = 0; i < e.getElementCount(); i++) collectLeaves(e.getElement(i), leaves);
  }
}
