package specman.clipboard;

import specman.EditException;
import specman.model.v002.AbstractStepModel_V002;

import java.util.List;

public interface PasteChangemarksAdjusterI {
  List<AbstractStepModel_V002> adjust(List<AbstractStepModel_V002> steps, boolean trackingOn) throws EditException;
}
