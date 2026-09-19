package specman.editarea.keylistener;

import org.jetbrains.annotations.NotNull;
import specman.clipboard.SpecmanTextTransferable;
import specman.editarea.TextEditArea;
import specman.editarea.markups.MarkedCharSequence;
import specman.editarea.markups.MarkupBackgroundStyleInitializer;
import specman.editarea.markups.MarkupRecovery;
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

import javax.swing.SwingUtilities;

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

  private void pasteFormatted(TextEditAreaModel_V002 model, Transferable specmanTransferable, Clipboard clipboard) {
    TextEditArea formattedPasteText = model2text(model);

    MarkedCharSequence marksBackup = assembleCombinedMarksBackup(formattedPasteText);

    copyPasteTextViaClipboard(formattedPasteText);

    SwingUtilities.invokeLater(() -> {
      List<Markup_V002> recovered = new MarkupRecovery(getWrappedDocument(), marksBackup).recover();
      new MarkupBackgroundStyleInitializer(textArea, recovered).styleChangedTextSections();
    });

    restoreClipboardContents(specmanTransferable, clipboard);
    // TODO: register pasted Steplinks in the referenced steps
    // TODO: mark pasted range as Added in current changeset if tracking is on
  }

  private @NotNull TextEditArea model2text(TextEditAreaModel_V002 model) {
    List<Markup_V002> nonChangeMarkups = model.markups != null
        ? model.markups.stream().filter(m -> !m.type.marksChange()).collect(Collectors.toList())
        : new ArrayList<>();
    TextEditAreaModel_V002 formattingOnly = new TextEditAreaModel_V002(
        model.text, model.plainText, nonChangeMarkups, (specman.ChangeInfo) null);
    TextEditArea formattedPasteText = new TextEditArea(formattingOnly, textArea.getFont());
    return formattedPasteText;
  }

  /** Uses the system clipboard as a transfer medium: puts the formatted content of
   * {@code formattedPasteText} onto the clipboard via JEditorPane's native copy action
   * (which produces a multi-flavor transferable preserving HTML structure, paragraph types,
   * and character attributes), then lets JEditorPane's paste action insert it at the caret.
   * This is the simplest way to copy the full HTML document structure between two JEditorPanes
   * without manual parsing. */
  private void copyPasteTextViaClipboard(TextEditArea formattedPasteText) {
    formattedPasteText.selectAll();
    formattedPasteText.copy();
    textArea.paste();
  }

  /** Restores the Specman text transferable on the system clipboard after
   * {@link #copyPasteTextViaClipboard} replaced it with the temp area's content.
   * Without this, a subsequent Ctrl+V would paste the temp area's HTML rather than
   * the original Specman clipboard content. */
  private void restoreClipboardContents(Transferable specmanTransferable, Clipboard clipboard) {
    clipboard.setContents(specmanTransferable, null);
  }

  /** Builds the expected character-markup sequence for the document state after the paste.
   * Combines: target chars before the caret + all chars from the pasted temp area (including
   * their Steplink markups) + target chars after the caret. {@link MarkupRecovery} then
   * maps this sequence onto the actual post-paste document, tolerating structural newlines
   * that JEditorPane inserts during the paste as unmatched whitespace. */
  private @NotNull MarkedCharSequence assembleCombinedMarksBackup(TextEditArea temp) {
    MarkedCharSequence targetBefore = textArea.findMarkups();
    MarkedCharSequence pastedContent = temp.findMarkups();
    int caretPos = getWrappedCaretPosition().toModel();

    MarkedCharSequence combined = new MarkedCharSequence();
    combined.append(targetBefore.subsequence(0, caretPos));
    combined.append(pastedContent);
    combined.append(targetBefore.subsequence(caretPos, targetBefore.size()));
    return combined;
  }
}
