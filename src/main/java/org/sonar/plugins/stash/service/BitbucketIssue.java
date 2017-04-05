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
    private final BitbucketDiff.Segment segment;
    private final BitbucketDiff.Line line;

    public int getPostLine() {
        return segment.isTypeOfContext() ? line.getSource() : line.getDestination();
    }

    static boolean isIssueBelongToSegment(BitbucketDiff.Segment segment, SonarIssue issue) {
        return segment.getLines().stream().mapToInt(line -> segment.isTypeOfContext() ? line.getSource() : line.getDestination())
                .anyMatch(line -> line == issue.getSafeLine());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BitbucketIssue && equalsSpecific((BitbucketIssue) obj);
    }

    private boolean equalsSpecific(BitbucketIssue issueObj) {
        return getPostLine() == issueObj.getPostLine() && Objects.equals(sonarIssue.key(), issueObj.sonarIssue.key());
    }

    @Override
    public int hashCode() {
        return getPostLine() * 31 + sonarIssue.key().hashCode() * 31;
    }
}
