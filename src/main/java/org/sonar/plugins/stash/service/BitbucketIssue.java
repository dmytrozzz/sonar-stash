package org.sonar.plugins.stash.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiff;
import org.sonar.plugins.stash.issue.SonarIssue;

/**
 * Created by dmytro.khaynas on 4/4/17.
 */
@AllArgsConstructor
@Getter
class BitbucketIssue {
    private final SonarIssue issue;
    private final BitbucketDiff.Segment segment;
    private final BitbucketDiff.Line line;

    static boolean isIssueBelongToSegment(BitbucketDiff.Segment segment, SonarIssue issue) {
        return segment.getLines().stream().mapToInt(line -> segment.isTypeOfContext() ? line.getSource() : line.getDestination())
                .anyMatch(line -> line == issue.getSafeLine());
    }
}
