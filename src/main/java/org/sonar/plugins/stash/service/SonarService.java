package org.sonar.plugins.stash.service;

import lombok.AllArgsConstructor;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.BitbucketPlugin;
import org.sonar.plugins.stash.client.SonarQubeClient;
import org.sonar.plugins.stash.config.StashPluginConfiguration;
import org.sonar.plugins.stash.issue.SonarIssue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by dmytro.khaynas on 3/30/17.
 */

@AllArgsConstructor
class SonarService {

    private static final Logger LOGGER = Loggers.get(BitbucketService.class);

    private final SonarQubeClient sonarqubeClient;
    private final StashPluginConfiguration configuration;
    private final ProjectIssues projectIssues;
    private final File baseDir;

    /**
     * Extract Code Coverage service to be published into the pull-request.
     */
    //CoverageIssuesReport getCoverageReport(PostJobContext context, String codeCoverageSeverity) {

    //    CoverageIssuesReport result = new CoverageIssuesReport();

//        try {
//            result = extractCoverageReport(context, codeCoverageSeverity);
//
//        } catch (SonarQubeClientException e) {
//            LOGGER.error("Unable to push SonarQube service to Stash: {}", e.getMessage());
//            LOGGER.debug("Stack trace", e);
//        }

    //      return result;
    //  }

    /**
     * Create issue service according to issue list generated during SonarQube
     * analysis.
     */
    List<SonarIssue> extractFilteredIssues(PostJobContext context) {
        return StreamSupport.stream(context.issues().spliterator(), false)
                .filter(this::isIssueNeedsInclude)
                .map(this::convertToBitbucketIssue)
                .collect(Collectors.toList());
    }

    private boolean isIssueNeedsInclude(PostJobIssue issue) {
        return true;
    }

    private SonarIssue convertToBitbucketIssue(PostJobIssue issue) {
//        Issue sonarIssue = StreamSupport.stream(projectIssues.issues().spliterator(), false)
//                .filter(i -> Objects.equals(i.componentKey(), issue.componentKey()) && Objects.equals(i.ruleKey(), issue.ruleKey()))
//                .findAny().orElse(null);
        List<String> taskSeverities = getReportedSeverities(configuration.getTaskIssueSeverityThreshold());

        String path = issue.inputComponent() != null && issue.inputComponent().isFile() ?
                baseDir.getName() + "/" + ((InputFile) issue.inputComponent()).relativePath() :
                issue.componentKey();

        return new SonarIssue(issue, path, taskSeverities.contains(issue.severity().name()), configuration.getSonarQubeURL());
    }

    /**
     * Get reported severities to create a task.
     *
     * @param threshold task issue severity threshold
     */
    private static List<String> getReportedSeverities(String threshold) {
        List<String> result = new ArrayList<>();

        // threshold == NONE, no severities reported
        if (!Objects.equals(threshold, BitbucketPlugin.SEVERITY_NONE)) {

            // INFO, MINOR, MAJOR, CRITICAL, BLOCKER
            boolean hit = false;
            for (String severity : BitbucketPlugin.SEVERITY_LIST) {

                if (hit || Objects.equals(severity, threshold)) {
                    result.add(severity);
                    hit = true;
                }
            }
        }

        return result;
    }

    /**
     * Extract Code Coverage service to be published into the pull-request.
     */
//    public CoverageIssuesReport extractCoverageReport(PostJobContext context, String codeCoverageSeverity) throws SonarQubeClientException {
//
//        CoverageIssuesReport result = new CoverageIssuesReport();
//
//        FileSystem fileSystem = inputFileCache.getFileSystem();
//        for (InputFile f : fileSystem.inputFiles(fileSystem.predicates().all())) {
//
//            Double linesToCover = null;
//            Double uncoveredLines = null;
//
//            Measure<Integer> linesToCoverMeasure = context.getMeasure(context.getResource(f), CoreMetrics.LINES_TO_COVER);
//            if (linesToCoverMeasure != null) {
//                linesToCover = linesToCoverMeasure.getValue();
//            }
//
//            Measure<Integer> uncoveredLinesMeasure = context.getMeasure(context.getResource(f), CoreMetrics.UNCOVERED_LINES);
//            if (uncoveredLinesMeasure != null) {
//                uncoveredLines = uncoveredLinesMeasure.getValue();
//            }
//
//            // get lines_to_cover, uncovered_lines
//            if ((linesToCover != null) && (uncoveredLines != null)) {
//                double previousCoverage = sonarqubeClient.getCoveragePerFile(sonarQubeProjectKey, f.relativePath());
//
//                CoverageIssue issue = new CoverageIssue(codeCoverageSeverity, f.relativePath());
//                issue.setLinesToCover(linesToCover);
//                issue.setUncoveredLines(uncoveredLines);
//                issue.setPreviousCoverage(previousCoverage);
//
//                result.add(issue);
//                LOGGER.debug(issue.getMessage());
//            }
//        }
//
//        // set previous project coverage from SonarQube server
//        double previousProjectCoverage = sonarqubeClient.getCoveragePerProject(sonarQubeProjectKey);
//        result.setPreviousProjectCoverage(previousProjectCoverage);
//
//        return result;
//    }
    public static SonarService fromConfig(StashPluginConfiguration config, ProjectIssues projectIssues, File baseDir) {
        return new SonarService(new SonarQubeClient(config.getSonarQubeURL()), config, projectIssues, baseDir);
    }
}
