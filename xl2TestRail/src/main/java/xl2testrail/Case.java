package xl2testrail;

import java.util.HashMap;
import java.util.Map;

public class Case {
    private final String title;
    private String preconditions;
    private final Map<Integer, Step> steps;

    public Case(String title) {
        this.title = title;
        preconditions = "";
        steps = new HashMap<>();
    }

    public void setPreconditions(String preconditions) {
        this.preconditions = preconditions;
    }

    public Map<Integer, Step> getSteps() {
        return steps;
    }

    public String getTitle() {
        return title;
    }

    public String getPreconditions() {
        return preconditions;
    }

    @Override
    public String toString() {
        return "Case{" +
                "title='" + title + '\'' +
                ", preconditions='" + preconditions + '\'' +
                ", steps=" + steps +
                '}';
    }
}
