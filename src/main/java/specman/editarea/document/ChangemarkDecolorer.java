package specman.editarea.document;

import specman.ChangeSet;
import specman.editarea.markups.MarkupSearchPurpose;
import specman.editarea.markups.TextMarkup;
import specman.model.v002.Markup_V002;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static specman.ChangeSet.STEPNUMBER_LINK_COLOR;
import static specman.graphics.Styles.TEXT_BACKGROUND_COLOR_STANDARD;
import static specman.editarea.document.WrappedDocumentUtil.*;

public class ChangemarkDecolorer {

  private final WrappedDocument doc;

  public ChangemarkDecolorer(WrappedDocument doc) {
    this.doc = doc;
  }

  public int decolor(List<DeletionRange> deletions, ChangeSet cs) {
    int changesMade = 0;
    for (WrappedElement e : doc.getRootElements()) {
      changesMade += decolorElement(e, deletions, cs);
    }
    return changesMade;
  }

  private int decolorElement(WrappedElement e, List<DeletionRange> deletions, ChangeSet cs) {
    int changesMade = 0;
    if (elementHatAenderungshintergrund(e, cs)) {
      int start = e.getStartOffset().toModel();
      int end = e.getEndOffset().toModel();
      if (end > start) {
        if (elementHatDurchgestrichenenText(e)) {
          deletions.add(new DeletionRange(start, end));
        }
        else {
          Color targetColor = stepnumberLinkChangedStyleSet(e, cs)
              ? STEPNUMBER_LINK_COLOR.color
              : TEXT_BACKGROUND_COLOR_STANDARD;
          MutableAttributeSet decolored = new SimpleAttributeSet();
          decolored.addAttributes(e.getAttributes());
          StyleConstants.setBackground(decolored, targetColor);
          doc.setCharacterAttributes(e.getStartOffset(), end - start, decolored, true);
          changesMade++;
        }
      }
    }
    for (int i = 0; i < e.getElementCount(); i++) {
      changesMade += decolorElement(e.getElement(i), deletions, cs);
    }
    return changesMade;
  }

  public List<Markup_V002> findMarkups(MarkupSearchPurpose searchPurpose) {
    List<Markup_V002> result = new ArrayList<>();
    for (WrappedElement e : doc.getRootElements()) {
      collectMarkups(e, result, searchPurpose);
      if (!result.isEmpty() && searchPurpose.stopAfterFirstMatch()) {
        break;
      }
    }
    return result;
  }

  private void collectMarkups(WrappedElement e, List<Markup_V002> result, MarkupSearchPurpose searchPurpose) {
    TextMarkup markup = TextMarkup.fromBackground(e);
    if (markup != null && markup.matches(searchPurpose)) {
      result.add(new Markup_V002(e.getStartOffset().toModel(), e.getEndOffset().toModel() - 1, markup));
      if (searchPurpose == MarkupSearchPurpose.FirstChangeOnly) {
        return;
      }
    }
    if (result.isEmpty() || searchPurpose == MarkupSearchPurpose.All) {
      for (int i = 0; i < e.getElementCount(); i++) {
        collectMarkups(e.getElement(i), result, searchPurpose);
        if (!result.isEmpty() && searchPurpose.stopAfterFirstMatch()) {
          break;
        }
      }
    }
  }
}
