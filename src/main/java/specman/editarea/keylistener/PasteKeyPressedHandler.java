package specman.editarea.keylistener;

import specman.clipboard.SpecmanTextTransferable;
import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;
import specman.model.v002.TextEditAreaModel_V002;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
          event.consume();
          TextEditAreaModel_V002 model = (TextEditAreaModel_V002)
              contents.getTransferData(SpecmanTextTransferable.SPECMAN_TEXT_FLAVOR);
          pasteFormatted(model, contents, clipboard);
          return;
        }
        // External content (Word etc.): strip to plain text to avoid messy HTML,
        // then let JEditorPane's default paste action insert it.
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

  private void pasteFormatted(TextEditAreaModel_V002 model, Transferable specmanTransferable, Clipboard clipboard) throws Exception {
    TextEditAreaModel_V002 formattingOnly = new TextEditAreaModel_V002(
        model.text, model.plainText, new ArrayList<>(), (specman.ChangeInfo) null);
    TextEditArea temp = new TextEditArea(formattingOnly, textArea.getFont());
    if (hasMultipleParagraphs(temp)) {
      // Multi-paragraph: use clipboard copy/paste — user accepts structural newlines
      temp.selectAll();
      temp.copy();
      textArea.paste();
      clipboard.setContents(specmanTransferable, null);
    }
    else {
      // Single paragraph: plain text only for now, no whitespace problems
      // TODO: restore character formatting (bold, italic etc.) for single-paragraph paste
      String plainText = (String)clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
      textArea.replaceSelection(plainText);
    }
  }

  private boolean hasMultipleParagraphs(TextEditArea temp) {
    WrappedDocument doc = temp.getWrappedDocument();
    WrappedPosition firstContent = firstNonNewline(doc, doc.start(), doc.end());
    WrappedPosition lastContent = lastNonNewline(doc, doc.end(), doc.start());
    if (firstContent == null || lastContent == null || !lastContent.greater(firstContent)) return false;
    WrappedElement firstPara = doc.getParagraphElement(firstContent);
    WrappedElement lastPara = doc.getParagraphElement(lastContent);
    return !firstPara.getStartOffset().equals(lastPara.getStartOffset());
  }

  private WrappedPosition firstNonNewline(WrappedDocument doc, WrappedPosition from, WrappedPosition to) {
    for (WrappedPosition p = from; !p.greater(to); p = p.inc()) {
      try { if (!"\n".equals(doc.getText(p, 1))) return p; } catch (Exception e) { break; }
    }
    return null;
  }

  private WrappedPosition lastNonNewline(WrappedDocument doc, WrappedPosition from, WrappedPosition to) {
    for (WrappedPosition p = from; !p.less(to); p = p.dec()) {
      try { if (!"\n".equals(doc.getText(p, 1))) return p; } catch (Exception e) { break; }
    }
    return null;
  }
}
