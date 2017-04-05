package org.sonar.plugins.stash.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.client.BitbucketClient;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketComment;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiff;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketPullRequest;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketUser;
import org.sonar.plugins.stash.config.PullRequestRef;
import org.sonar.plugins.stash.config.StashCredentials;
import org.sonar.plugins.stash.config.StashPluginConfiguration;
import org.sonar.plugins.stash.exceptions.StashConfigurationException;
import org.sonar.plugins.stash.issue.SonarIssue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dmytro.khaynas on 3/30/17.
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class BitbucketService {

    private static final Logger LOGGER = Loggers.get(BitbucketService.class);

    @Getter
    private final BitbucketClient bitbucketClient;
    private final File baseDir;

    /**
     * Push SonarQube report into the Bitbucket pull-request as comments.
     */
    void postSonarDiffReport(List<SonarIssue> issues, List<BitbucketDiff> diffs) {
        Set<BitbucketIssue> openIssuesForPR = diffs.stream()
                .filter(this::isDiffScannable)
                //flatter all to BitbucketIssue and then filter (cleaner syntax)
                .flatMap(diff -> diff.getHunks().stream()
                        .flatMap(hunk -> hunk.getSegments().stream()
                                .filter(BitbucketDiff.Segment::isNotRemove)
                                .flatMap(segment -> segment.getLines().stream()
                                        .flatMap(line -> issues.stream().map(issue ->
                                                new BitbucketIssue(issue, diff, segment, line))))))
                .filter(BitbucketIssue::isIssueBelongsToDiffAndNew)
                .filter(BitbucketIssue::isIssueBelongsToLine)
                .distinct()
                .collect(Collectors.toSet());

        openIssuesForPR.stream().parallel().forEach(this::postBitbucketIssue);

        resolveAndReopenTasks(diffs, openIssuesForPR);
        LOGGER.info("New SonarQube issues have been reported to Stash.");
    }

    /**
     * Returns true if diff 1. belongs to current project analysed (by base dir compared) and 2. is not binary
     */
    private boolean isDiffScannable(BitbucketDiff diff) {
        return diff.hasCode() && Objects.equals(diff.getParent(), baseDir.getName());
    }

    /**
     * Will post a comment to Bitbucket diff.
     * But only if current file and row is in context of PR (in diff)
     */
    private void postBitbucketIssue(BitbucketIssue issue) {
        try {
            BitbucketComment comment = bitbucketClient.postCommentOnPRLine(issue.getSonarIssue().prettyString(), issue.getSonarIssue().getPath(), issue.getPostLine(), issue.getSegment().getType());

            LOGGER.debug("Comment \"{}\" has been created ({}) on file {} ({})", issue.getSonarIssue().key(), issue.getSegment().getType(),
                    issue.getSonarIssue().getPath(), issue.getPostLine());

            if (issue.getSonarIssue().isTaskNeeded()) {
                bitbucketClient.postTaskOnComment(issue.getSonarIssue().message(), comment.getId());
                LOGGER.debug("Comment \"{}\" has been linked to a Stash task", comment.getId());
            }
        } catch (IOException e) {
            LOGGER.error("Unable to link SonarQube issue to Stash" + issue.getSonarIssue().key(), e);
        }
    }

    /**
     * Will resolve tasks that is not now relevant
     *
     * @param diffs           diffs of pr
     * @param openIssuesForPR
     */
    private void resolveAndReopenTasks(List<BitbucketDiff> diffs, Set<BitbucketIssue> openIssuesForPR) {
        BitbucketUser currentUser = bitbucketClient.getUser();

        diffs.stream()
                .filter(this::isDiffScannable)
                .flatMap(BitbucketDiff::getCommentsStream)
                // only if 1. published by the current Bitbucket user
                .filter(comment -> currentUser.equals(comment.getAuthor()) &&
                        //only comments that not contained in issues (probably resolved)
                        openIssuesForPR.stream().noneMatch(issue ->
                                Objects.equals(issue.getSonarIssue().prettyString(), comment.getText())))
                .distinct()
                .flatMap(comment -> comment.getTasks().stream())
                .filter(task -> task.getAuthor().equals(currentUser) && task.isOpen())
                .parallel()
                .forEach(bitbucketClient::resolveTask);

        List<BitbucketComment> projectComments = diffs.stream()
                .filter(this::isDiffScannable)
                .flatMap(BitbucketDiff::getCommentsStream)
                // only if 1. published by the current Bitbucket user
                .filter(comment -> currentUser.equals(comment.getAuthor()) &&
                        //only comments that is contained in issues (probably reopened)
                        openIssuesForPR.stream().anyMatch(issue ->
                                Objects.equals(issue.getSonarIssue().prettyString(), comment.getText())))
                .distinct()
                .collect(Collectors.toList());

        projectComments.stream()
                .filter(comment -> comment.getTasks() == null || comment.getTasks().isEmpty())
                .parallel()
                .forEach(comment -> bitbucketClient.postTaskOnComment(comment.getText(), comment.getId()));

        projectComments.stream().flatMap(comment -> comment.getTasks().stream())
                .filter(task -> task.getAuthor().equals(currentUser) && !task.isOpen())
                .parallel()
                .forEach(bitbucketClient::reopenTask);
    }

    /**
     * Add a reviewer to the current pull-request.
     */
    void addMeAsReviewer() {
        try {
            BitbucketPullRequest pullRequest = bitbucketClient.getPullRequest();
            // user not yet in reviewer list
            BitbucketUser reviewer = pullRequest.getReviewer(getBitbucketClient().getLogin());
            if (reviewer == null) {
                ArrayList<BitbucketUser> reviewers = new ArrayList<>(pullRequest.getReviewers());
                BitbucketUser user = bitbucketClient.getUser();
                reviewers.add(user);

                bitbucketClient.addPullRequestReviewer(pullRequest.getVersion(), reviewers);

                LOGGER.info("User \"{}\" is now a reviewer of the pull-request {}", user, String.valueOf(bitbucketClient.getPullRequest()));
            }
        } catch (IOException e) {
            LOGGER.error("Unable to add a new reviewer to the pull-request", e);
        }
    }

    /**
     * Reset all comments linked to a pull-request.
     */
    void resetComments(BitbucketDiff.BitbucketDiffs diffReport, BitbucketUser sonarUser) {
        diffReport.getDiffs().stream()
                .flatMap(BitbucketDiff::getCommentsStream)
                // only if 1. published by the current Bitbucket user 2. doesn't contain tasks which cannot be deleted
                .filter(comment -> sonarUser.equals(comment.getAuthor()) && comment.isDeleteable())
                .distinct().parallel()
                .forEach(bitbucketClient::deletePullRequestComment);
        LOGGER.info("SonarQube issues reported to Stash by user \"{}\" have been reset", sonarUser.getName());
    }

    static BitbucketService fromConfig(StashPluginConfiguration config, File baseDir) throws StashConfigurationException {
        String stashURL = config.getStashURL();
        int stashTimeout = config.getStashTimeout();

        StashCredentials stashCredentials = config.getCredentials();
        PullRequestRef pr = config.getPullRequest();
        return new BitbucketService(new BitbucketClient(stashURL, stashCredentials, pr, stashTimeout), baseDir);
    }

    /**
     * Push Code Coverage service into the pull-request as comments.
     */
//    void postCoverageReport(String sonarQubeURL, CoverageIssuesReport coverageReport, BitbucketDiffs diffReport,
//                            String threshold) {
//        try {
//            if (!coverageReport.isEmpty()) {
//
//                postCommentPerIssue(sonarQubeURL, coverageReport.getLoweredIssues(), diffReport, threshold);
//
//                LOGGER.info("Code coverage service has been reported to Stash.");
//            }
//        } catch (IOException e) {
//            LOGGER.error("Unable to push code coverage service to Stash: {}", e.getMessage());
//            LOGGER.debug("Stack trace", e);
//        }
//    }

    /**
     * Post SQ analysis overview on Stash
     */
//    void postAnalysisOverview(PullRequestRef pr, String sonarQubeURL, int issueThreshold, List<SonarIssue> issues) {
//        bitbucketClient.postCommentOnPR(MarkdownPrinter
//                .printReportMarkdown(pr, bitbucketClient.getBaseUrl(), sonarQubeURL, issueReport, coverageReport, issueThreshold));
//        LOGGER.info("SonarQube analysis overview has been reported to Stash.");
//    }
}
