package org.sonar.plugins.stash.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiff;
import org.sonar.plugins.stash.issue.SonarIssue;

import java.util.Objects;

/**
 * Created by dmytro.khaynas on 4/4/17.
 */
@AllArgsConstructor
@Getter
class BitbucketIssue {
    private final SonarIssue sonarIssue;
    private final BitbucketDiff diff;
    private final BitbucketDiff.Segment segment;
    private final BitbucketDiff.Line line;

    public int getPostLine() {
        return segment.isTypeOfContext() ? line.getSource() : line.getDestination();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BitbucketIssue && equalsSpecific((BitbucketIssue) obj);
    }

    private boolean equalsSpecific(BitbucketIssue issueObj) {
        return getPostLine() == issueObj.getPostLine() && Objects.equals(sonarIssue.key(), issueObj.sonarIssue.key());
    }

    /**
     * Return true if 1. diff and issue have equal path to analysed file and 2. Hasn't yet been posted to diff
     */
    boolean isIssueBelongsToDiffAndNew() {
        return diff.getPath().equals(sonarIssue.getPath()) && diff.getCommentsStream().noneMatch(comment -> comment.getText().equals(sonarIssue.prettyString()));
    }

    /**
     * Return true if issue belongs to specific segment
     */
    boolean isIssueBelongsToLine() {
        return getPostLine() == sonarIssue.getSafeLine();
    }

    @Override
    public int hashCode() {
        return getPostLine() * 31 + sonarIssue.key().hashCode() * 31;
    }
}
