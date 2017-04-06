package org.sonar.plugins.stash.issue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.sonar.api.batch.postjob.issue.PostJobIssue;

import java.util.Objects;

/**
 * Created by dmytro.khaynas on 3/31/17.
 */
@AllArgsConstructor
public class SonarIssue {
    @Delegate
    private final PostJobIssue sonarIssue;
    @Getter
    private final String path;
    @Getter
    private final boolean commentAndTaskNeeded;
    private final String sonarQubeURL;

    public int getSafeLine() {
        return sonarIssue.line() != null ? sonarIssue.line() : 0;
    }

    public String prettyString() {
        return IssuePrinter.printIssueMarkdown(sonarQubeURL, this);
    }

    String getKey() {
        return sonarIssue.key();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SonarIssue && Objects.equals(key(), sonarIssue.key());
    }

    @Override
    public int hashCode() {
        return sonarIssue.key().hashCode();
    }
}
