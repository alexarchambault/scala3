package dotty.tools;

import dotty.tools.dotc.core.Contexts;
import org.junit.jupiter.api.*;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public abstract class ContextEscapeDetection {
    public static class TestContext{
        public TestContext(WeakReference<Contexts.Context> context, String testName) {
            this.context = context;
            this.testName = testName;
        }

        public final WeakReference<Contexts.Context> context;
        public final String testName;

    }
    public static final List<TestContext> contexts = new LinkedList<TestContext>();

    public abstract Contexts.Context getCtx();

    public abstract void clearCtx();

    @BeforeEach
    public synchronized void stealContext() {
        contexts.add(new TestContext(new WeakReference<Contexts.Context>(this.getCtx()), this.getClass().getName()));
    }

    @AfterEach
    public synchronized void clearContext() {
        this.clearCtx();
    }
}
