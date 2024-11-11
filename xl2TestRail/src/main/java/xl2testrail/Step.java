package xl2testrail;

public record Step(String description, String expected) {

    @Override
    public String toString() {
        return "Step{" +
                "description='" + description + '\'' +
                ", expected='" + expected + '\'' +
                '}';
    }
}
