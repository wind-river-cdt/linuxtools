package org.eclipse.linuxtools.lttng.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.lttng.tests.control.LTTngSyntheticEventProviderTest;
import org.eclipse.linuxtools.lttng.tests.control.LTTngSyntheticEventProviderTextTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventContentTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventFieldTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventReferenceTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventTypeTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngTimestampTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniEventTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniMarkerFieldTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniMarkerTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniTraceTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniTracefileTest;
import org.eclipse.linuxtools.lttng.tests.model.LTTngTreeNodeTest;
import org.eclipse.linuxtools.lttng.tests.state.TestStateManager;
import org.eclipse.linuxtools.lttng.tests.state.experiment.StateExperimentManagerTextTest;
import org.eclipse.linuxtools.lttng.tests.state.resource.LTTngStateResourceTest;
import org.eclipse.linuxtools.lttng.tests.trace.LTTngExperimentTest;
import org.eclipse.linuxtools.lttng.tests.trace.LTTngTextTraceTest;
import org.eclipse.linuxtools.lttng.tests.trace.LTTngTraceTest;

public class AllLTTngCoreTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllLTTngCoreTests.class.getName());
        // $JUnit-BEGIN$

        // Event
        suite.addTestSuite(LttngTimestampTest.class);
        suite.addTestSuite(LttngEventFieldTest.class);
        suite.addTestSuite(LttngEventContentTest.class);
        suite.addTestSuite(LttngEventReferenceTest.class);
        suite.addTestSuite(LttngEventTypeTest.class);
        suite.addTestSuite(LttngEventTest.class);

        // JNI
        suite.addTestSuite(JniTraceTest.class);
        suite.addTestSuite(JniTracefileTest.class);
        suite.addTestSuite(JniEventTest.class);
        suite.addTestSuite(JniMarkerTest.class);
        suite.addTestSuite(JniMarkerFieldTest.class);

        // Trace
        suite.addTestSuite(LTTngTextTraceTest.class);
        suite.addTestSuite(LTTngTraceTest.class);
        suite.addTestSuite(LTTngExperimentTest.class);

        // Control
        suite.addTestSuite(LTTngSyntheticEventProviderTest.class);
        suite.addTestSuite(LTTngSyntheticEventProviderTextTest.class);

        // Model
        suite.addTestSuite(LTTngTreeNodeTest.class);

        // State
        suite.addTestSuite(TestStateManager.class);
        suite.addTestSuite(StateExperimentManagerTextTest.class);
        // suite.addTestSuite(AbsStateUpdate.class);
        // suite.addTestSuite(StateAfterUpdateFactory.class);
        // suite.addTestSuite(StateAfterUpdateHandlers.class);
        // suite.addTestSuite(StateBeforeUpdateFactory.class);
        // suite.addTestSuite(StateBeforeUpdateHandlers.class);
        suite.addTestSuite(LTTngStateResourceTest.class);

        // $JUnit-END$
        return suite;
    }

}
