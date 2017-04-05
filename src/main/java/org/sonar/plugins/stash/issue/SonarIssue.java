package org.sonar.plugins.stash.issue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.issue.Issue;

/**
 * Created by dmytro.khaynas on 3/31/17.
 */
@AllArgsConstructor
public class SonarIssue {
    @Delegate
    private final PostJobIssue sonarIssue;
    //private final Issue issue;
    @Getter
    private final String path;
    @Getter
    private final boolean taskNeeded;
    private final String sonarQubeURL;

    public int getSafeLine() {
        return sonarIssue.line() != null ? sonarIssue.line() : 0;
    }

    public String prettyString() {
        return IssuePrinter.printIssueMarkdown(sonarQubeURL, this);
    }

    public String getKey() {
        return /*issue != null ? issue.key() :*/ sonarIssue.key();
    }
}
