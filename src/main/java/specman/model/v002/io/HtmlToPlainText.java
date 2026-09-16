package specman.model.v002.io;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

/** Converting HTML to plain text is based on headless JEditorPanes to ensure that it works exactly
 * identical in both UI and headless sanitizer. */
class HtmlToPlainText {
    static final HTMLEditorKit HTML_EDITOR_KIT = new HTMLEditorKit();

    static String convert(String html) throws Exception {
        JEditorPane ed = new JEditorPane();
        ed.setEditorKit(HTML_EDITOR_KIT);
        ed.setText(html);
        SwingUtilities.invokeAndWait(() -> {});
        return ed.getDocument().getText(0, ed.getDocument().getLength()).replaceAll("^\\n", "");
    }
}
