package specman.ops;

import specman.EditException;
import specman.clipboard.SpecmanClipboardContent;
import specman.clipboard.SpecmanTransferable;
import specman.model.v002.AbstractStepModel_V002;
import specman.model.v002.io.ModelParser_V002;
import specman.model.v002.io.ModelParseException;
import specman.model.v002.io.StepIdRemapper_V002;
import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static specman.Specman.editor;
import static specman.view.RelativeStepPosition.After;

public class PasteStepsOp extends AbstractADBLSpecmanOp {

  private final AbstractSchrittView referenceStep;

  public PasteStepsOp(SpecmanOpContext context, AbstractSchrittView referenceStep) {
    super(context);
    this.referenceStep = referenceStep;
  }

  @Override
  void execute() throws EditException {
    try {
      SpecmanClipboardContent content = readClipboard();
      if (content != null) {
        List<AbstractStepModel_V002> stepModels = new ModelParser_V002().parseSteps(content.serializedSteps);
        ensureUniqueStepIds(stepModels);
        addSteps(stepModels, content.instanceId);
      }
    }
    catch (ModelParseException ex) {
      throw new EditException("Clipboard content cannot be pasted as a Specman step:\n" + ex.getMessage());
    }
  }

  private void addSteps(List<AbstractStepModel_V002> stepModels, String clipboardInstanceId) {
    SchrittSequenzView parent = referenceStep.getParent();
    AbstractSchrittView reference = referenceStep;
    for (AbstractStepModel_V002 stepModel : stepModels) {
      AbstractSchrittView newStep = AbstractSchrittView.baueSchrittViewFromV2(parent, stepModel);
      parent.insertStep(newStep, After, reference);
      parent.renumberFollowingSteps(reference);
      adaptChangemarks(newStep, clipboardInstanceId);
      newStep.viewsNachinitialisieren();
      newStepPostInit(newStep);
      reference = newStep;
    }
  }

  private void ensureUniqueStepIds(List<AbstractStepModel_V002> models) {
    Set<String> existingIds = editor().listAllSteps().stream()
      .map(AbstractSchrittView::getId)
      .collect(Collectors.toSet());
    StepIdRemapper_V002.remapIfNeeded(models, existingIds);
  }

  /**
   * Adapts change-tracking markings of a pasted step according to its origin.
   * <p>
   * Rules for steps coming from the <b>same instance</b>:
   * <ul>
   *   <li>Text markings inside the step are preserved as-is.</li>
   *   <li>Sub-steps keep their markings regardless of the change-tracking state.</li>
   *   <li>The top-level step itself:
   *     <ul>
   *       <li>Change tracking ON: marked as added in the current changeset,
   *           even if it was already marked as added in a different changeset in the clipboard.</li>
   *       <li>Change tracking OFF: marking from the clipboard is kept unchanged.</li>
   *     </ul>
   *   </li>
   * </ul>
   * Steps from a <b>different instance</b> are not yet handled here and will follow separate rules.
   */
  private void adaptChangemarks(AbstractSchrittView newStep, String clipboardInstanceId) {
    boolean sameInstance = editor().instanceId().equals(clipboardInstanceId);
    if (sameInstance && aenderungenVerfolgen()) {
      // Mark only the top-level step as added in the current changeset;
      // sub-steps and text markings remain exactly as they were in the clipboard
      newStep.markAsAddedInCurrentChangeset();
    }
  }

  private SpecmanClipboardContent readClipboard() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (clipboard.isDataFlavorAvailable(SpecmanTransferable.SPECMAN_STEPS_FLAVOR)) {
        return (SpecmanClipboardContent) clipboard.getData(SpecmanTransferable.SPECMAN_STEPS_FLAVOR);
      }
      String text = (String) clipboard.getData(DataFlavor.stringFlavor);
      return (text != null && !text.isBlank()) ? new SpecmanClipboardContent(null, text) : null;
    }
    catch (Exception ex) {
      return null;
    }
  }
}

