package org.broadinstitute.hellbender.tools.copynumber;

import org.broadinstitute.hellbender.CommandLineProgramTest;
import org.broadinstitute.hellbender.cmdline.StandardArgumentDefinitions;
import org.broadinstitute.hellbender.cmdline.argumentcollections.IntervalArgumentCollection;
import org.broadinstitute.hellbender.testutils.ArgumentsBuilder;
import org.broadinstitute.hellbender.tools.copynumber.arguments.CopyNumberStandardArgument;
import org.broadinstitute.hellbender.utils.IntervalMergingRule;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Integration tests for {@link GermlineCNVCaller}.
 */
public final class GermlineCNVCallerIntegrationTest extends CommandLineProgramTest {
    private static final String GCNV_SIM_DATA_DIR = toolsTestDir + "copynumber/gcnv-sim-data/";
    private static final File[] TEST_COUNT_FILES = IntStream.range(0, 20)
            .mapToObj(n -> new File(GCNV_SIM_DATA_DIR + String.format("SAMPLE_%03d_counts.tsv", n)))
            .toArray(File[]::new);
    private static final File CONTIG_PLOIDY_CALLS_OUTPUT_DIR = new File(GCNV_SIM_DATA_DIR + "contig-ploidy-calls/");
    private static final File SIM_INTERVAL_LIST_SUBSET_FILE = new File(GCNV_SIM_DATA_DIR + "sim_intervals_subset.interval_list");
    private static final File OUTPUT_DIR = createTempDir("test-germline-cnv");

    /**
     * Run the tool in the COHORT mode for all 20 samples on a small subset of intervals
     */
    @Test(groups = {"python"})
    public void testCohortWithoutIntervalAnnotations() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(TEST_COUNT_FILES).forEach(argsBuilder::addInput);
        argsBuilder.add(GermlineCNVCaller.RUN_MODE_LONG_NAME, GermlineCNVCaller.RunMode.COHORT.name())
                .add("L", SIM_INTERVAL_LIST_SUBSET_FILE.getAbsolutePath())
                .add(GermlineCNVCaller.CONTIG_PLOIDY_CALLS_DIRECTORY_LONG_NAME,
                        CONTIG_PLOIDY_CALLS_OUTPUT_DIR.getAbsolutePath())
                .add(StandardArgumentDefinitions.OUTPUT_LONG_NAME, OUTPUT_DIR.getAbsolutePath())
                .add(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-germline-cnv-cohort")
                .add(IntervalArgumentCollection.INTERVAL_MERGING_RULE_LONG_NAME, IntervalMergingRule.OVERLAPPING_ONLY.toString());
        runCommandLine(argsBuilder);
    }

    /**
     * Run the tool in CASE mode for the first 5 samples using the model generated by
     * {@link #testCohortWithoutIntervalAnnotations()}
     */
    @Test(groups = {"python"}, dependsOnMethods = "testCohortWithoutIntervalAnnotations")
    public void testCase() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(TEST_COUNT_FILES, 0, 5).forEach(argsBuilder::addInput);
        argsBuilder.add(GermlineCNVCaller.RUN_MODE_LONG_NAME, GermlineCNVCaller.RunMode.CASE.name())
                .add(GermlineCNVCaller.CONTIG_PLOIDY_CALLS_DIRECTORY_LONG_NAME,
                        CONTIG_PLOIDY_CALLS_OUTPUT_DIR.getAbsolutePath())
                .add(CopyNumberStandardArgument.MODEL_LONG_NAME,
                        new File(OUTPUT_DIR, "test-germline-cnv-cohort-model").getAbsolutePath())
                .add(StandardArgumentDefinitions.OUTPUT_LONG_NAME, OUTPUT_DIR.getAbsolutePath())
                .add(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-germline-cnv-case");
        runCommandLine(argsBuilder);
        Assert.assertTrue(true);
    }

    @Test(groups = {"python"}, expectedExceptions = IllegalArgumentException.class)
    public void testCaseWithoutModel() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(TEST_COUNT_FILES, 0, 5).forEach(argsBuilder::addInput);
        argsBuilder.add(GermlineCNVCaller.RUN_MODE_LONG_NAME, GermlineCNVCaller.RunMode.CASE.name())
                .add(GermlineCNVCaller.CONTIG_PLOIDY_CALLS_DIRECTORY_LONG_NAME,
                        CONTIG_PLOIDY_CALLS_OUTPUT_DIR.getAbsolutePath())
                .add(StandardArgumentDefinitions.OUTPUT_LONG_NAME, OUTPUT_DIR.getAbsolutePath())
                .add(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-germline-cnv-case");
        runCommandLine(argsBuilder);
    }

    @Test(groups = {"python"}, enabled = false)
    public void testCohortWithInputModel() {
    }

    @Test(groups = {"python"}, enabled = false)
    public void testCohortWithAnnotatedIntervals() {
    }
}
