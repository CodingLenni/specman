package specman.model.v002;

import specman.model.v002.io.ModelParseException;
import specman.model.v002.io.ModelParser_V002;
import specman.model.v002.io.ModelSerializer_V002;

import java.util.List;

/** Diagnoses whether change=(added/removed, cs) survives a serialize→parse round-trip. */
public class ChangeInfoRoundTripTest {

    static final String INPUT =
        "// {\"specmanVersion\":\"1.3.0\",\"modelType\":\"specman.model.v002.DiagramModel_V002\"}\n" +
        "// Specman: test\n" +
        "settings { width=700 zoom=100 changeModeEnabled=true changeSetName=yellow }\n" +
        "mainSequence {\n" +
        "    simple(1, aa11bb22, `added step`, change=(added, yellow));\n" +
        "    simple(2, cc33dd44, `removed step`, change=(removed, yellow));\n" +
        "    simple(3, ee55ff66, `plain step`);\n" +
        "}\n";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Parse original ===");
        DiagramModel_V002 m1 = parse(INPUT);
        printChangeInfos(m1.queryAllSteps());

        String serialized = new ModelSerializer_V002().serialize(m1);
        System.out.println("\n=== Serialized (mainSequence block only) ===");
        serialized.lines()
            .dropWhile(l -> !l.contains("mainSequence"))
            .forEach(System.out::println);

        System.out.println("=== Parse round-trip ===");
        DiagramModel_V002 m2 = parse(serialized);
        printChangeInfos(m2.queryAllSteps());
    }

    private static DiagramModel_V002 parse(String text) throws ModelParseException {
        return new ModelParser_V002().parse(text);
    }

    private static void printChangeInfos(List<AbstractStepModel_V002> steps) {
        for (AbstractStepModel_V002 s : steps) {
            ChangeInfoModel_V002 ci = s.changeInfo;
            String desc = ci == null ? "null"
                : ci.changetype + " / " + ci.changeset;
            System.out.println("  step " + s.stepNumber + " id=" + s.id + "  changeInfo=" + desc);
        }
    }
}
