package specman.ops;

import specman.EditException;
import specman.editarea.InteractiveStepFragment;
import specman.view.AbstractSchrittView;
import specman.view.BreakSchrittView;

import javax.swing.JOptionPane;
import java.util.List;

public class CutStepOp extends AbstractADBLSpecmanOp {

  private final AbstractSchrittView step;
  private final InteractiveStepFragment initiatingFragment;

  public CutStepOp(SpecmanOpContext context, AbstractSchrittView step, InteractiveStepFragment initiatingFragment) {
    super(context);
    this.step = step;
    this.initiatingFragment = initiatingFragment;
  }

  @Override
  void execute() throws EditException {
    if (userConfirmedCutDespiteExternalCatches()) {
      new CopyStepOp(step).run();
      deleteStepADBL(step, initiatingFragment);
    }
  }

  private boolean userConfirmedCutDespiteExternalCatches() {
    List<BreakSchrittView> externalBreaks = step.queryExternallyLinkedBreakSteps();
    if (externalBreaks.isEmpty()) {
      return true;
    }
    int count = externalBreaks.size();
    int result = showConfirmDialog(
        "The step contains " + count + " break step(s) whose catch sequence(s) are located outside the step itself. " +
        "Cutting will permanently remove those catch sequence(s).\n" +
        "Consider moving the step via drag & drop instead.\n\nCut anyway?",
        "Linked catch sequences will be lost",
        JOptionPane.OK_CANCEL_OPTION);
    return result == JOptionPane.OK_OPTION;
  }

}
