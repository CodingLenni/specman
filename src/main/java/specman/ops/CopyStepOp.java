package specman.ops;

import specman.model.v002.AbstractStepModel_V002;
import specman.model.v002.io.ModelSerializer_V002;
import specman.view.AbstractSchrittView;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import static specman.Specman.editor;

public class CopyStepOp {

  private final AbstractSchrittView step;

  public CopyStepOp(AbstractSchrittView step) {
    this.step = step;
  }

  public void run() {
    AbstractStepModel_V002 model = step.generiereModel(true);
    String text = new ModelSerializer_V002().serializeStep(model, editor().instanceId());
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
  }
}
