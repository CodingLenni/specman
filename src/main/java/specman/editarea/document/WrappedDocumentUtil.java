package specman.editarea.document;

import specman.ChangeSet;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;

import static specman.ChangeSet.STEPNUMBER_LINK_COLOR;

public class WrappedDocumentUtil {

  public record DeletionRange(int start, int end) {}

  // ---- Change detection -------------------------------------------------------

  public static boolean elementHatDurchgestrichenenText(WrappedElement e) {
    AttributeSet attr = e.getAttributes();
    return StyleConstants.isStrikeThrough(attr);
  }

  public static boolean elementHatAenderungshintergrund(WrappedElement e) {
    return elementHatAenderungshintergrund(e, null);
  }

  public static boolean elementHatAenderungshintergrund(WrappedElement e, ChangeSet cs) {
    String cssColor = getBackgroundColorFromElement(e);
    if (cs != null) {
      return cs.isAnyBackground(cssColor);
    }
    return ChangeSet.isAnyChangeSetBackground(cssColor);
  }

  public static boolean stepnumberLinkNormalStyleSet(WrappedElement element) {
    String color = getBackgroundColorFromElement(element);
    return STEPNUMBER_LINK_COLOR.isBackground(color);
  }

  public static boolean stepnumberLinkChangedStyleSet(WrappedElement element) {
    return stepnumberLinkChangedStyleSet(element, null);
  }

  public static boolean stepnumberLinkChangedStyleSet(WrappedElement element, ChangeSet cs) {
    String color = getBackgroundColorFromElement(element);
    if (color == null) {
      return false;
    }
    if (cs != null) {
      return color.equalsIgnoreCase(cs.stepnumberLinkHtmlColor());
    }
    return ChangeSet.isAnyStepnumberLinkChangedBackground(color);
  }

  public static boolean stepnumberLinkStyleSet(WrappedElement element) {
    return element != null &&
      (stepnumberLinkNormalStyleSet(element) || stepnumberLinkChangedStyleSet(element));
  }

  static String getBackgroundColorFromElement(WrappedElement element) {
    Object backgroundColorValue = element.getAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return backgroundColorValue != null ? backgroundColorValue.toString() : null;
  }
}
