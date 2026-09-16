package specman.model.v002.io;

import specman.StepNumber;
import specman.model.v002.AbstractStepModel_V002;
import specman.model.v002.CatchSequenceModel_V002;
import specman.model.v002.CoCatchModel_V002;
import specman.model.v002.StepSequenceModel_V002;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Resolves step-ID collisions in a pasted model fragment and assigns new IDs to ensure
 * uniqueness all over the model. */
public class StepIdRemapper_V002 {

  public static void remapIfNeeded(List<AbstractStepModel_V002> models, Set<String> existingIds) {
    List<AbstractStepModel_V002> allSteps = new ArrayList<>();
    for (AbstractStepModel_V002 step : models) {
      step.addStepRecursively(allSteps);
    }

    Map<String, String> idMap = new LinkedHashMap<>();
    for (AbstractStepModel_V002 step : allSteps) {
      if (step.id != null && existingIds.contains(step.id) && !idMap.containsKey(step.id)) {
        idMap.put(step.id, AbstractStepModel_V002.generateId());
      }
    }
    if (idMap.isEmpty()) return;

    for (AbstractStepModel_V002 step : allSteps) {
      step.id = remap(step.id, idMap);
      step.sourceStepId = remap(step.sourceStepId, idMap);
    }

    for (AbstractStepModel_V002 step : models) {
      remapSubSequences(step, idMap);
    }
  }

  private static void remapSubSequences(AbstractStepModel_V002 step, Map<String, String> idMap) {
    for (NumberedSubSequence_V002 numbered : step.subSequencesFor(StepNumber.EMPTY)) {
      remapSequence(numbered.sequence, idMap);
    }
  }

  private static void remapSequence(StepSequenceModel_V002 seq, Map<String, String> idMap) {
    if (seq == null) return;
    seq.id = remap(seq.id, idMap);
    if (seq.catchArea != null && seq.catchArea.catchSequences != null) {
      for (CatchSequenceModel_V002 cs : seq.catchArea.catchSequences) {
        remapSequence(cs, idMap);
        if (cs.coCatches != null) {
          for (CoCatchModel_V002 cc : cs.coCatches) {
            cc.breakStepId = remap(cc.breakStepId, idMap);
          }
        }
      }
    }
    if (seq.steps != null) {
      for (AbstractStepModel_V002 step : seq.steps) {
        remapSubSequences(step, idMap);
      }
    }
  }

  private static String remap(String id, Map<String, String> idMap) {
    return id != null ? idMap.getOrDefault(id, id) : null;
  }
}
