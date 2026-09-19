package specman.editarea;

import javax.swing.text.html.HTML;

public interface HTMLTags {
  String HTML_INTRO = toIntro(HTML.Tag.HTML);
  String HTML_OUTRO = toOutro(HTML.Tag.HTML);
  String HEAD_INTRO = toIntro(HTML.Tag.HEAD);
  String HEAD_OUTRO = toOutro(HTML.Tag.HEAD);
  String BODY_INTRO = toIntro(HTML.Tag.BODY);
  String BODY_OUTRO = toOutro(HTML.Tag.BODY);
  String SPAN_INTRO = toIntro(HTML.Tag.SPAN);
  String SPAN_OUTRO = toOutro(HTML.Tag.SPAN);
  String DIV_INTRO = toIntro(HTML.Tag.DIV);
  String DIV_OUTRO = toOutro(HTML.Tag.DIV);
  String PRE_INTRO = toIntro(HTML.Tag.PRE);
  String PRE_OUTRO = toOutro(HTML.Tag.PRE);

  static String toIntro(HTML.Tag tag) {
    return "<" + tag.toString().toLowerCase() + ">";
  }

  static String toOutro(HTML.Tag tag) {
    return "</" + tag.toString().toLowerCase() + ">";
  }

  /** Strips all HTML/HEAD/BODY wrapper tags and an optional outer {@code <div>},
   * returning only the inline body content. The outer {@code <div>} is stripped because
   * JEditorPane wraps single-paragraph content in one, which would become an unwanted
   * block-level break when pasting into another area. */
  static String bodyContent(String html) {
    String body = html
        .replace(HTML_INTRO, "").replace(HTML_OUTRO, "")
        .replace(HEAD_INTRO, "").replace(HEAD_OUTRO, "")
        .replace(BODY_INTRO, "").replace(BODY_OUTRO, "")
        .trim();
    String trimmed = body.trim();
    if (trimmed.startsWith(DIV_INTRO) && trimmed.endsWith(DIV_OUTRO)) {
      return trimmed.substring(DIV_INTRO.length(), trimmed.length() - DIV_OUTRO.length()).strip();
    }
    return body;
  }

  /** Wraps body content in minimal HTML/BODY structure for use with JEditorPane. */
  static String wrapAsHtml(String bodyContent) {
    return HTML_INTRO + BODY_INTRO + bodyContent + BODY_OUTRO + HTML_OUTRO;
  }

}
