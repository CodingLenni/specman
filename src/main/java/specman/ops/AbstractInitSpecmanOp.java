package specman.ops;

import javax.swing.*;
import java.io.File;

public abstract class AbstractInitSpecmanOp extends AbstractSpecmanOp {

  static final Object[] UNSAVED_CHANGES_OPTIONS = {"Save", "Discard", "Cancel"};

  protected AbstractInitSpecmanOp(SpecmanOpContext context) {
    super(context);
  }

  public static boolean confirmDiscardUnsavedChanges(String filename, Runnable saveAction) {
    int result = JOptionPane.showOptionDialog(
        null,
        "The document '" + filename + "' has unsaved changes.\nYour changes will be lost if you don't save them.",
        "Unsaved Changes",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        UNSAVED_CHANGES_OPTIONS,
        UNSAVED_CHANGES_OPTIONS[0]);
    if (result == JOptionPane.CLOSED_OPTION || result == 2) {
      return false;
    }
    if (result == 0) {
      saveAction.run();
    }
    return true;
  }

  protected boolean confirmDiscardUnsavedChanges() {
    if (!hasUnsavedChanges()) {
      return true;
    }
    File current = getDiagrammDatei();
    String filename = current != null ? current.getName() : "Unknown";
    return confirmDiscardUnsavedChanges(filename, () -> context.diagrammSpeichern(false));
  }

}
