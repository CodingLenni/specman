package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.TextEditAreaAccessMixin;
import specman.editarea.document.WrappedPosition;
import specman.view.AbstractSchrittView;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;

import static specman.ChangeSet.changeset;
import static specman.Specman.editor;

public class AbstractKeyHandler implements TextEditAreaAccessMixin {
  protected final TextEditArea textArea;

  protected AbstractKeyHandler(TextEditArea textArea) {
    this.textArea = textArea;
  }

  @Override
  public TextEditArea textArea() { return textArea; }

  protected boolean shouldPreventActionInsideStepnumberLink() {
    if (stepnumberLinkStyleSetAt(getWrappedSelectionStart()) || stepnumberLinkStyleSetAt(getWrappedSelectionEnd())) {
      if (isCaretInsideSelection()) {
        return true;
      }

      for (WrappedPosition i = getWrappedSelectionStart(); i.less(getWrappedSelectionEnd()); i = i.inc()) {
        if (stepnumberLinkStyleSetAt(i)) {
          if (getStartOffsetFromPosition(i).less(getWrappedSelectionStart()) ||
            getEndOffsetFromPosition(i).greater(getWrappedSelectionEnd())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected boolean isCaretInsideSelection() {
    WrappedPosition linkStyleStart = getStartOffsetFromPosition(getWrappedSelectionEnd());
    WrappedPosition linkStyleEnd = getEndOffsetFromPosition(getWrappedSelectionEnd());
    return getWrappedSelectionStart().equals(getWrappedSelectionEnd()) &&
      getWrappedSelectionEnd().less(linkStyleEnd) &&
      getWrappedSelectionStart().greater(linkStyleStart);
  }

  protected boolean skipToStepnumberLinkEnd() {
    WrappedPosition selectionEnd = getWrappedSelectionEnd();
    if (stepnumberLinkStyleSetAt(selectionEnd)) {
      setCaretPosition(getEndOffsetFromPosition(selectionEnd).unwrap());
      return true;
    }
    return false;
  }

  protected boolean skipToStepnumberLinkStart() {
    WrappedPosition selectionStart = getWrappedSelectionStart();
    if (!selectionStart.isZero() && stepnumberLinkStyleSetAt(selectionStart.dec())) {
      setCaretPosition(getStartOffsetFromPosition(selectionStart.dec()).unwrap());
      return true;
    }
    return false;
  }

  protected void markRangeAsDeleted(WrappedPosition deleteStart, int deleteLength, MutableAttributeSet deleteStyle) {
    getWrappedDocument().setCharacterAttributes(deleteStart, deleteLength, deleteStyle, false);
  }

  public void markSelectedTextAsDeletedInModificationMode() {
    if (!editor().aenderungenVerfolgen()) {
      return;
    }
    AbstractSchrittView textOwner = editor().findeSchritt(textArea);
    if (textOwner == null || !isEditable()) {
      return;
    }
    WrappedPosition selectionStart = getWrappedSelectionStart();
    WrappedPosition selectionEnd = getWrappedSelectionEnd();
    if (selectionStart.equals(selectionEnd)) {
      return;
    }
    if (stepnumberLinkNormalStyleSetAt(selectionStart)) {
      markRangeAsDeleted(selectionStart, selectionEnd.distance(selectionStart), changeset().getDeletedStepnumberLinkStyle());
    }
    else {
      markRangeAsDeleted(selectionStart, selectionEnd.distance(selectionStart), changeset().getDeletedStyle());
    }
    setSelectionStart(selectionEnd.unwrap());
    StyledEditorKit k = getEditorKit();
    MutableAttributeSet inputAttributes = k.getInputAttributes();
    StyleConstants.setStrikeThrough(inputAttributes, false);
  }

}
