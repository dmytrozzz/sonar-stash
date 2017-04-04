package org.sonar.plugins.stash.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.client.BitbucketClient;
import org.sonar.plugins.stash.client.bitbucket.models.*;
import org.sonar.plugins.stash.client.bitbucket.models.request.Comment;
import org.sonar.plugins.stash.config.PullRequestRef;
import org.sonar.plugins.stash.config.StashCredentials;
import org.sonar.plugins.stash.config.StashPluginConfiguration;
import org.sonar.plugins.stash.exceptions.StashConfigurationException;
import org.sonar.plugins.stash.issue.BitbucketIssue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dmytro.khaynas on 3/30/17.
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BitbucketService {

    private static final Logger LOGGER = Loggers.get(BitbucketService.class);

    @Getter
    private final BitbucketClient bitbucketClient;
    private final File baseDir;

    /**
     * Push SonarQube report into the Bitbucket pull-request as comments.
     */
    void postSonarDiffReport(List<BitbucketIssue> issues, List<BitbucketDiff> diffs) {
        diffs.stream().collect(Collectors.toMap(Function.identity(),
                diff -> issues.stream().filter(issue -> isIssueToPost(diff, issue))
                        .collect(Collectors.toList())))
                //.entrySet().stream()
                //.filter(entry -> !entry.getValue().isEmpty())
                .forEach(this::handleDiff);

        resolveAndReopenTasks(diffs, issues);
        LOGGER.info("New SonarQube issues have been reported to Stash.");
    }

    private boolean isIssueToPost(BitbucketDiff diff, BitbucketIssue issue) {
        return isIssueBelongsToDiff(diff, issue) &&
                //2. diff has not yet comments for this issue
                diff.getCommentsStream().noneMatch(comment -> comment.getText().equals(issue.prettyString()));
    }

    private boolean isIssueBelongsToDiff(BitbucketDiff diff, BitbucketIssue issue) {
        //1. they have equal path to file
        return diff.getPath().equals(issue.getPath());
    }

    private void handleDiff(BitbucketDiff diff, List<BitbucketIssue> issues) {
        diff.getHunks().stream().flatMap(hunk -> hunk.getSegments().stream())
                .filter(segment -> !segment.getType().equals(Comment.REMOVED_ISSUE_TYPE))
                .collect(Collectors.toMap(Function.identity(),
                        segment -> issues.stream().filter(issue -> isIssueBelongToSegment(segment, issue)).collect(Collectors.toList())))
                //.entrySet().stream()
                //.filter(entry -> !entry.getValue().isEmpty())
                //.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((segment, issueBySegment) -> issueBySegment.forEach(issue -> postBitbucketIssue(segment, issue)));
    }

    private boolean isIssueBelongToSegment(BitbucketDiff.Segment segment, BitbucketIssue issue) {
        return segment.getLines().stream().mapToInt(line -> segment.isTypeOfContext() ? line.getSource() : line.getDestination())
                .anyMatch(line -> line == issue.getSafeLine());
    }

    /**
     * Will post a comment to Bitbucket diff.
     * But only if current file and row is in context of PR (in diff)
     */
    private void postBitbucketIssue(BitbucketDiff.Segment segment, BitbucketIssue issue) {
        try {
            BitbucketComment comment = bitbucketClient.postCommentOnPRLine(issue.prettyString(), issue.getPath(), issue.getSafeLine(), segment.getType());

            LOGGER.debug("Comment \"{}\" has been created ({}) on file {} ({})", issue.key(), segment.getType(),
                    issue.getPath(), issue.getSafeLine());

            if (issue.isTaskNeeded()) {
                bitbucketClient.postTaskOnComment(issue.message(), comment.getId());
                LOGGER.debug("Comment \"{}\" has been linked to a Stash task", comment.getId());
            }
        } catch (IOException e) {
            LOGGER.error("Unable to link SonarQube issue to Stash" + issue.key(), e);
        }
    }

    /**
     * Will resolve tasks that is not now relevant
     *
     * @param diffs     diffs of pr
     * @param allIssues
     */
    private void resolveAndReopenTasks(List<BitbucketDiff> diffs, List<BitbucketIssue> allIssues) {
        BitbucketUser currentUser = bitbucketClient.getUser();

        List<BitbucketIssue> diffedIssues = allIssues.stream()
                .filter(issue -> diffs.stream().anyMatch(diff -> isIssueBelongsToDiff(diff, issue)))
                .collect(Collectors.toList());

        diffs.stream()
                //only belongs to this project
                .filter(diff -> Objects.equals(diff.getParent(), baseDir.getName()))
                .flatMap(BitbucketDiff::getCommentsStream)
                // only if 1. published by the current Bitbucket user
                .filter(comment -> currentUser.equals(comment.getAuthor()) &&
                        //only comments that not contained in issues (probably resolved)
                        diffedIssues.stream().noneMatch(issue -> Objects.equals(issue.prettyString(), comment.getText())))
                .distinct()
                .flatMap(comment -> comment.getTasks().stream())
                .filter(task -> task.getAuthor().equals(currentUser) && task.isOpen())
                .parallel()
                .forEach(bitbucketClient::resolveTask);

        List<BitbucketComment> projectComments = diffs.stream()
                .filter(diff -> Objects.equals(diff.getParent(), baseDir.getName()))
                .flatMap(BitbucketDiff::getCommentsStream)
                // only if 1. published by the current Bitbucket user
                .filter(comment -> currentUser.equals(comment.getAuthor()) &&
                        //only comments that is contained in issues (probably reopened)
                        diffedIssues.stream().anyMatch(issue -> Objects.equals(issue.prettyString(), comment.getText())))
                .distinct()
                .collect(Collectors.toList());

        projectComments.stream()
                .filter(comment -> comment.getTasks() == null || comment.getTasks().isEmpty())
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
    void resetComments(BitbucketDiffs diffReport, BitbucketUser sonarUser) {
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
//    void postAnalysisOverview(PullRequestRef pr, String sonarQubeURL, int issueThreshold, List<BitbucketIssue> issues) {
//        bitbucketClient.postCommentOnPR(MarkdownPrinter
//                .printReportMarkdown(pr, bitbucketClient.getBaseUrl(), sonarQubeURL, issueReport, coverageReport, issueThreshold));
//        LOGGER.info("SonarQube analysis overview has been reported to Stash.");
//    }
}
