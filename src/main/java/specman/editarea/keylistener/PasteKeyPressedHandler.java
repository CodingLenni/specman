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
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

import static specman.ChangeSet.changeset;

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
      // Multi-paragraph: formatting comes free via copy/paste; apply changeset color manually
      int caretBefore = textArea.getCaretPosition();
      temp.selectAll();
      temp.copy();
      textArea.paste();
      if (isTrackingChanges()) {
        int caretAfter = textArea.getCaretPosition();
        ((StyledDocument) textArea.getDocument())
            .setCharacterAttributes(caretBefore, caretAfter - caretBefore,
                changeset().textBackground(), false);
      }
      clipboard.setContents(specmanTransferable, null);
    }
    else {
      // Single paragraph: insert plain text then restore character formatting
      textArea.replaceSelection("");
      int insertStart = textArea.getCaretPosition();
      String plainText = (String)clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
      // replaceSelection uses the StyledEditorKit's current input attributes, which
      // aenderungsStilSetzenWennNochNichtVorhanden() already set to the changeset color
      // in change tracking mode — so inserted text is automatically colored correctly.
      textArea.replaceSelection(plainText);
      applyCharacterFormatting(temp, insertStart);
      // TODO: apply Steplink backgrounds
    }
  }

  private void applyCharacterFormatting(TextEditArea source, int insertStart) {
    WrappedDocument sourceDoc = source.getWrappedDocument();
    StyledDocument targetDoc = (StyledDocument) textArea.getDocument();
    List<WrappedElement> leaves = new ArrayList<>();
    collectLeaves(sourceDoc.getRootElements().get(0), leaves); // skip bidi root
    int targetPos = insertStart;
    for (WrappedElement leaf : leaves) {
      int length = leaf.getEndOffset().distance(leaf.getStartOffset());
      try {
        String text = sourceDoc.getText(leaf.getStartOffset(), length);
        if ("\n".equals(text)) continue;
        targetDoc.setCharacterAttributes(targetPos, text.length(), leaf.getAttributes(), false);
        targetPos += text.length();
      }
      catch (Exception ignored) {}
    }
  }

  private void collectLeaves(WrappedElement e, List<WrappedElement> leaves) {
    if (e.getElementCount() == 0) leaves.add(e);
    else for (int i = 0; i < e.getElementCount(); i++) collectLeaves(e.getElement(i), leaves);
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
