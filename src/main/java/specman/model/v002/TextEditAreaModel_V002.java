package specman.model.v002;

import com.fasterxml.jackson.annotation.JsonIgnore;
import specman.ChangeInfo;

import java.util.ArrayList;
import java.util.List;

public class TextEditAreaModel_V002 extends AbstractEditAreaModel_V002 {
    public String text;
    public String plainText;
    public List<Markup_V002> markups;

    @Deprecated public TextEditAreaModel_V002() { // For Jackson only
        text = null;
        plainText = null;
        markups = null;
        changeInfo = null;
    }

    public TextEditAreaModel_V002(String text) {
        this(text, text, new ArrayList<>(), (ChangeInfo) null);
    }

    public TextEditAreaModel_V002(String text, String plainText, List<Markup_V002> markups, ChangeInfo changeInfo) {
        this.text = text;
        this.plainText = plainText;
        this.markups = markups;
        this.changeInfo = ChangeInfoModel_V002.from(changeInfo);
    }

    public TextEditAreaModel_V002(String text, String plainText, List<Markup_V002> markups, ChangeInfoModel_V002 changeInfo) {
        this.text = text;
        this.plainText = plainText;
        this.markups = markups;
        this.changeInfo = changeInfo;
    }

    @JsonIgnore
    public boolean isEmpty() { return text.isEmpty(); }
}
