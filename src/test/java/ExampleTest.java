import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.*;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

public class ExampleTest extends ExecutionContext {

    public void vertex1() {
        System.out.println("vertex1");
    }

    public void edge1() {
        System.out.println("edge1");
    }

    public void vertex2() {
        System.out.println("vertex2");
    }

    public void vertex3() {
        throw new RuntimeException();
    }

    public boolean isFalse() {
        return false;
    }

    public boolean isTrue() {
        return true;
    }

    public void myAction() {
        System.out.println("Action called");
    }

    @Test
    public void success() {
        Vertex start = new Vertex();
        Model model = new Model().addEdge(new Edge()
                .setName("edge1")
                .setGuard(new Guard("isTrue()"))
                .setSourceVertex(start
                        .setName("vertex1"))
                .setTargetVertex(new Vertex()
                        .setName("vertex2"))
                .addAction(new Action("myAction();")));
        this.setModel(model.build());
        this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        setNextElement(start);
        Machine machine = new SimpleMachine(this);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
    }

    @Test(expected = MachineException.class)
    public void failure() {
        Vertex start = new Vertex();
        Model model = new Model().addEdge(new Edge()
                .setName("edge1")
                .setGuard(new Guard("isFalse()"))
                .setSourceVertex(start
                        .setName("vertex1"))
                .setTargetVertex(new Vertex()
                        .setName("vertex2")));
        this.setModel(model.build());
        this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        setNextElement(start);
        Machine machine = new SimpleMachine(this);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
    }

    @Test
    public void exception() {
        Vertex start = new Vertex();
        Model model = new Model().addEdge(new Edge()
                .setName("edge1")
                .setGuard(new Guard("isTrue()"))
                .setSourceVertex(start
                        .setName("vertex3"))
                .setTargetVertex(new Vertex()
                        .setName("vertex2")));
        this.setModel(model.build());
        this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        setNextElement(start);
        Machine machine = new SimpleMachine(this);
        Assert.assertThat(getExecutionStatus(), is(ExecutionStatus.NOT_EXECUTED));
        try {
            while (machine.hasNextStep()) {
                machine.getNextStep();
                Assert.assertThat(getExecutionStatus(), is(ExecutionStatus.EXECUTING));
            }
        } catch (Throwable t) {
            Assert.assertTrue(MachineException.class.isAssignableFrom(t.getClass()));
            Assert.assertThat(getExecutionStatus(), is(ExecutionStatus.FAILED));
        }
    }
}