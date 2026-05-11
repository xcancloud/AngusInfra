package cloud.xcan.angus.plugin.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link SampleResults#enrichFailureMeta(SampleResult)} 单元测试。
 */
class SampleResultsTest {

    @Test
    void success_result_isReturnedAsIs() {
        SampleResult r = SampleResult.builder()
                .sampleName("ok")
                .success(true)
                .build();
        assertSame(r, SampleResults.enrichFailureMeta(r));
        assertNull(r.getFailureKind());
    }

    @Test
    void nullResult_returnsNull() {
        assertNull(SampleResults.enrichFailureMeta(null));
    }

    @Test
    void alreadyEnriched_isIdempotent() {
        SampleResult r = SampleResult.builder()
                .sampleName("x")
                .success(false)
                .failureKind(SampleResult.FailureKind.TIMEOUT)
                .build();
        assertSame(r, SampleResults.enrichFailureMeta(r));
        assertEquals(SampleResult.FailureKind.TIMEOUT, r.getFailureKind());
    }

    @Test
    void assertionFailure_populatesFailedAssertionFields() {
        AssertionResult passed = AssertionResult.passed("ok", "STATUS_CODE");
        AssertionResult failed = AssertionResult.failed("body-check", "BODY_CONTAINS",
                "hello", "world", "expected 'hello' but got 'world'");
        SampleResult r = SampleResult.builder()
                .sampleName("step-1")
                .success(false)
                .assertions(List.of(passed, failed))
                .build();

        SampleResult enriched = SampleResults.enrichFailureMeta(r);
        assertEquals(SampleResult.FailureKind.ASSERTION_FAILED, enriched.getFailureKind());
        assertEquals("body-check", enriched.getFailedAssertionName());
        assertEquals("hello", enriched.getFailedAssertionExpected());
        assertEquals("world", enriched.getFailedAssertionActual());
    }

    @Test
    void timeoutErrorCode_classifiedAsTimeout() {
        SampleResult r = SampleResult.builder()
                .sampleName("step-1")
                .success(false)
                .errorCode("CONNECT_TIMEOUT")
                .errorMessage("connect timeout")
                .build();
        SampleResult enriched = SampleResults.enrichFailureMeta(r);
        assertEquals(SampleResult.FailureKind.TIMEOUT, enriched.getFailureKind());
    }

    @Test
    void scriptErrorCode_classifiedAsScriptError() {
        SampleResult r = SampleResult.builder()
                .sampleName("step-1")
                .success(false)
                .errorCode("SCRIPT_ERROR")
                .build();
        assertEquals(SampleResult.FailureKind.SCRIPT_ERROR,
                SampleResults.enrichFailureMeta(r).getFailureKind());
    }

    @Test
    void unknownErrorCode_defaultsToTransportError() {
        SampleResult r = SampleResult.builder()
                .sampleName("step-1")
                .success(false)
                .errorCode("CONNECTION_REFUSED")
                .build();
        assertEquals(SampleResult.FailureKind.TRANSPORT_ERROR,
                SampleResults.enrichFailureMeta(r).getFailureKind());
    }

    @Test
    void noErrorCode_defaultsToTransportError() {
        SampleResult r = SampleResult.builder()
                .sampleName("step-1")
                .success(false)
                .build();
        assertEquals(SampleResult.FailureKind.TRANSPORT_ERROR,
                SampleResults.enrichFailureMeta(r).getFailureKind());
    }

    @Test
    void toBuilder_roundTripsNewFields() {
        SampleResult original = SampleResult.builder()
                .sampleName("step-1")
                .success(false)
                .failureKind(SampleResult.FailureKind.ASSERTION_FAILED)
                .failedAssertionName("a")
                .failedAssertionExpected("e")
                .failedAssertionActual("x")
                .build();
        SampleResult copy = original.toBuilder().sampleName("step-2").build();
        assertEquals("step-2", copy.getSampleName());
        assertTrue(!copy.isSuccess());
        assertEquals(SampleResult.FailureKind.ASSERTION_FAILED, copy.getFailureKind());
        assertEquals("a", copy.getFailedAssertionName());
        assertEquals("e", copy.getFailedAssertionExpected());
        assertEquals("x", copy.getFailedAssertionActual());
    }
}
