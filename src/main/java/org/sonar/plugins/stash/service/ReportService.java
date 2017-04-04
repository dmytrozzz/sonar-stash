package org.sonar.plugins.stash.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiffs;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketUser;
import org.sonar.plugins.stash.config.StashPluginConfiguration;
import org.sonar.plugins.stash.exceptions.StashConfigurationException;
import org.sonar.plugins.stash.issue.BitbucketIssue;

import java.io.File;
import java.util.List;

/**
 * Created by dmytro.khaynas on 3/31/17.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportService {

    private static final Logger LOGGER = Loggers.get(ReportService.class);
    public static final String STACK_TRACE = "Exception stack trace";

    private final SonarService sonarService;
    private final BitbucketService bitbucketService;

    private final StashPluginConfiguration pluginConfiguration;

    public static ReportService create(StashPluginConfiguration configuration, ProjectIssues projectIssues, File baseDir) throws StashConfigurationException {
        return new ReportService(SonarService.fromConfig(configuration, projectIssues, baseDir), BitbucketService.fromConfig(configuration, baseDir), configuration);
    }

    public void reportFromSonarToBitbucket(PostJobContext context) {
        LOGGER.info("Start report analyse");

        try {
            BitbucketUser bitbucketUser = bitbucketService.getBitbucketClient().getUser();

            // Get all changes exposed from Stash differential view of the pull-request
            BitbucketDiffs diffReport = bitbucketService.getBitbucketClient().getPullRequestDiffReport();

            if (bitbucketUser != null && diffReport != null) {
                preReportActions(bitbucketUser, diffReport);

                List<BitbucketIssue> issues = sonarService.extractFilteredIssues(context);
                //CoverageIssuesReport coverageReport = codeCoverageReport(context);
                doReportAction(diffReport, issues);

                postReportActions(issues);
            } else {
                LOGGER.error("Either user or diff can't be reached User: " + bitbucketUser + " / Diff: " + diffReport);
            }
        } catch (StashConfigurationException e) {
            LOGGER.error("Unable to push SonarQube service to Stash: {}", e.getMessage());
            LOGGER.debug(STACK_TRACE, e);
        }
    }

//    private CoverageIssuesReport codeCoverageReport(PostJobContext context) {
//        if (!Objects.equals(pluginConfiguration.getCodeCoverageSeverity(), BitbucketPlugin.SEVERITY_NONE)) {
//            return sonarService.getCoverageReport(context, pluginConfiguration.getCodeCoverageSeverity());
//        }
//        return new CoverageIssuesReport();
//    }

    private void preReportActions(BitbucketUser user, BitbucketDiffs diffReport) {
        // if requested, reset all comments linked to the pull-request
        if (pluginConfiguration.resetComments()) {
            bitbucketService.resetComments(diffReport, user);
        }
        if (pluginConfiguration.canApprovePullRequest()) {
            bitbucketService.addMeAsReviewer();
        }
    }

    private void doReportAction(BitbucketDiffs diffReport, List<BitbucketIssue> issues) throws StashConfigurationException {


        //int issueNumber = issues.size();// + coverageReport.countLoweredIssues();
        //int issueTotal = issueReport.countIssues();

        // if threshold exceeded, do not push issue list to Stash
        //if (isIssueThresholdExceeded(issueNumber)) {
        //LOGGER.warn("Too many issues detected ({}/{}): Issues cannot be displayed in Diff view",
        //        issueTotal, issueThreshold);
        //} else {
        bitbucketService.postSonarDiffReport(issues, diffReport.getDiffs());
        //bitbucketService.postCoverageReport(pluginConfiguration.getSonarQubeURL(), coverageReport, diffReport, pluginConfiguration.getTaskIssueSeverityThreshold());
        //}

//        if (pluginConfiguration.includeAnalysisOverview()) {
//            bitbucketService.postAnalysisOverview(pluginConfiguration.getPullRequest(), pluginConfiguration.getSonarQubeURL(), issues);
//        }
    }

    private boolean isIssueThresholdExceeded(int issuesNumber) throws StashConfigurationException {
        return issuesNumber >= pluginConfiguration.getIssueThreshold();
    }

    private void postReportActions(List<BitbucketIssue> issues) {
        int issueNumber = issues.size();// + coverageReport.countLoweredIssues();

        // Some local definitions
        boolean canApprovePullrequest = pluginConfiguration.canApprovePullRequest();

        // if no new issues and coverage is improved, plugin approves the pull-request
        if (canApprovePullrequest && (issueNumber == 0) /*&& (coverageReport.getEvolution() >= 0)*/) {
            bitbucketService.getBitbucketClient().approvePullRequest();
        } else if (canApprovePullrequest && ((issueNumber != 0) /*|| coverageReport.getEvolution() < 0)*/)) {
            bitbucketService.getBitbucketClient().resetPullRequestApproval();
        }
    }
}
