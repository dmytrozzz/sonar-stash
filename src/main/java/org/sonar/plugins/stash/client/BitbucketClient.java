package org.sonar.plugins.stash.client;

import lombok.Getter;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.client.bitbucket.BitbucketApi;
import org.sonar.plugins.stash.client.bitbucket.BitbucketPrApi;
import org.sonar.plugins.stash.client.bitbucket.models.*;
import org.sonar.plugins.stash.client.bitbucket.models.request.Comment;
import org.sonar.plugins.stash.client.bitbucket.models.request.CommentTask;
import org.sonar.plugins.stash.config.PullRequestRef;
import org.sonar.plugins.stash.config.StashCredentials;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitbucketClient {

    private static final Logger LOGGER = Loggers.get(BitbucketClient.class);

    @Getter
    private final String baseUrl;
    private final BitbucketPrApi bitbucketPrApi;
    private final BitbucketApi bitbucketApi;
    @Getter
    private final StashCredentials credentials;
    private final PullRequestRef pr;

    private static final String REST_API = "/rest/api/1.0/";
    private static final String PR_PATH = "projects/%s/repos/%s/pull-requests/%s/";

    public BitbucketClient(String baseUrl, StashCredentials credentials, PullRequestRef pr, int stashTimeout) {
        this.baseUrl = baseUrl;
        this.credentials = credentials;
        this.pr = pr;
        this.bitbucketPrApi = ClientFactory
                .buildRetrofit(baseUrl + REST_API + String.format(PR_PATH, pr.getProject(), pr.getRepository(), pr.getId()), credentials
                        .getLogin(), credentials.getPassword(), stashTimeout).build()
                .create(BitbucketPrApi.class);
        this.bitbucketApi = ClientFactory.buildRetrofit(baseUrl + REST_API, credentials.getLogin(), credentials.getPassword(), stashTimeout).build()
                .create(BitbucketApi.class);
    }

    public String getLogin() {
        return credentials.getLogin();
    }

    /* GET */

    public BitbucketPullRequest getPullRequest() throws IOException {
        return bitbucketPrApi.getPR().execute().body();
    }

    public StashCommentReport getPullRequestComments(String path) {
        StashCommentReport result = new StashCommentReport();
        long start = 0;
        boolean isLastPage = false;
        while (!isLastPage) {
            try {
                StashCommentReport partReport = bitbucketPrApi.getPRComments(path, start)
                        .execute().body();
                result.add(partReport);
                // Stash pagination: check if you get all comments linked to the pull-request
                isLastPage = partReport.isLastPage();
                start = partReport.getNextPageStart();
            } catch (IOException e) {
                LOGGER.error("Unable to get comment linked to " + pr.getId(), e);
            }
        }
        return result;
    }

    /**
     * Get all changes exposed through the Stash pull-request.
     */
    public BitbucketDiff.BitbucketDiffs getPullRequestDiffReport() {
        try {
            BitbucketDiff.BitbucketDiffs result = bitbucketPrApi.getPRDiffs().execute().body();
            LOGGER.debug("Stash differential service retrieved from pull request {} #{}", pr.getRepository(), pr.getId());
            return result;
        } catch (IOException e) {
            LOGGER.error("Unable to get Stash differential service from Stash", e);
        }
        return null;
    }

    /**
     * Get user who published the SQ analysis in Stash.
     */
    public BitbucketUser getUser() {
        return getUser(getLogin());
    }

    /**
     * Get user who published the SQ analysis in Stash.
     */
    public BitbucketUser getUser(String userSlug) {
        try {
            BitbucketUser user = bitbucketApi.getUser(userSlug).execute().body();
            LOGGER.debug("SonarQube reviewer {} identified in Stash", user);
            return user;
        } catch (IOException e) {
            LOGGER.error(String.format("Unable to get SonarQube reviewer %s from Stash", userSlug), e);
        }
        return null;
    }

    /* EDIT */

    public void postCommentOnPR(String report) {
        Map<String, String> body = new HashMap<>(1);
        body.put("text", report);
        try {
            bitbucketPrApi.postCommentOnPR(body).execute();
        } catch (IOException e) {
            LOGGER.error("Unable to post a comment to " + pr.getId(), e);
        }
    }

    public BitbucketComment postCommentOnPRLine(String message, String path, int line, String type)
            throws IOException {
        Comment comment = new Comment(message, line, type, path);
        return bitbucketPrApi.postCommentOnLine(comment).execute().body();
    }

    public void addPullRequestReviewer(long pullRequestVersion, List<BitbucketUser> reviewers) {
        BitbucketPullRequest bitbucketPullRequest = new BitbucketPullRequest(pr, pullRequestVersion, reviewers);
        try {
            bitbucketPrApi.editPR(bitbucketPullRequest).execute();
        } catch (IOException e) {
            LOGGER.error("Unable to update pull-request " + pr.getId(), e);
        }
    }

    public void postTaskOnComment(String message, Long commentId) {
        try {
            bitbucketApi.postTaskOnComment(new CommentTask(commentId, message)).execute();
        } catch (IOException e) {
            LOGGER.error("Unable to post a task on comment " + commentId, e);
        }
    }

    private void resolveTask(BitbucketTask task, String resolution) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("id", String.valueOf(task.getId()));
            data.put("state", resolution);
            bitbucketApi.editTask(task.getId(), data).execute();
        } catch (IOException e) {
            LOGGER.error("Unable to edit a task " + task.getId(), e);
        }
    }

    public void resolveTask(BitbucketTask task) {
        resolveTask(task, BitbucketTask.RESOLVED);
    }

    public void reopenTask(BitbucketTask task) {
        resolveTask(task, BitbucketTask.OPEN);
    }

    public void approvePullRequest() {
        try {
            bitbucketPrApi.approvePR().execute();
            LOGGER.info("Pull-request {} ({}/{}) APPROVED by user \"{}\"", pr.getId(), pr.getProject(), pr.getRepository(), getLogin());
        } catch (IOException e) {
            LOGGER.error("Unable to approve pull-request " + pr.getId(), e);
        }
    }

    /* DELETE */
    public void deletePullRequestComment(BitbucketComment comment) {
        try {
            comment.getTasks().forEach(this::deleteTaskOnComment);
            bitbucketPrApi.deleteCommentFromPR(comment.getId(), comment.getVersion()).execute();
            LOGGER.debug(String.format("Comment %d deleted from pull-request %d", comment.getId(), pr.getId()));
        } catch (IOException e) {
            LOGGER.error(String.format("Unable to delete comment %d from pull-request %d", comment.getId(), pr.getId()), e);
        }
    }

    public void resetPullRequestApproval() {
        try {
            bitbucketPrApi.resetApproveOnPR().execute();
            LOGGER.info("Pull-request {} ({}/{}) NOT APPROVED by user \"{}\"",
                    pr.getId(), pr.getProject(), pr.getRepository(), getLogin());
        } catch (IOException e) {
            LOGGER.error("Unable to reset pull-request approval", e);
        }
    }

    public void deleteTaskOnComment(BitbucketTask task) {
        try {
            bitbucketApi.deleteTask(task.getId()).execute();
        } catch (IOException e) {
            LOGGER.error("Unable to delete task " + task.getId(), e);
        }
    }
}
