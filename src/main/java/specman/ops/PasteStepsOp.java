package specman.ops;

import specman.EditException;
import specman.model.v002.AbstractStepModel_V002;
import specman.model.v002.io.ModelParser_V002;
import specman.model.v002.io.ModelParseException;
import specman.model.v002.io.StepIdRemapper_V002;
import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import java.awt.Toolkit;
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
      String text = readClipboard();
      if (text == null) return;

      List<AbstractStepModel_V002> models = new ModelParser_V002().parseSteps(text);

      Set<String> existingIds = editor().listAllSteps().stream()
          .map(AbstractSchrittView::getId)
          .collect(Collectors.toSet());
      StepIdRemapper_V002.remapIfNeeded(models, existingIds);

      AbstractSchrittView reference = referenceStep;
      for (AbstractStepModel_V002 model : models) {
        SchrittSequenzView parent = reference.getParent();
        AbstractSchrittView newStep = AbstractSchrittView.baueSchrittViewFromV2(parent, model);
        parent.insertStep(newStep, After, reference);
        parent.renumberFollowingSteps(reference);
        newStep.viewsNachinitialisieren();
        newStepPostInit(newStep);
        reference = newStep;
      }
    }
    catch (ModelParseException ex) {
      throw new EditException("Clipboard content cannot be pasted as a Specman step:\n" + ex.getMessage());
    }
  }

  private String readClipboard() {
    try {
      String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
      return (text != null && !text.isBlank()) ? text : null;
    }
    catch (Exception ex) {
      return null;
    }
  }
}
