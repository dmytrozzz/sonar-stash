package org.sonar.plugins.stash.issue;

import org.sonar.api.batch.postjob.issue.PostJobIssue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dmytro.khaynas on 3/31/17.
 */
public class IssueUtils {
    public List<PostJobIssue> getIssuesBySeverity(List<PostJobIssue> issues, String severity) {
        return issues.stream()
                .filter(issue -> Objects.equals(severity, issue.severity().name()))
                .collect(Collectors.toList());
    }

    /**
     * Extract rule list according to a severity.
     */
    public Map<String, PostJobIssue> getUniqueRulesBySeverity(List<PostJobIssue> issues, String severity) {
        return getIssuesBySeverity(issues, severity)
                .stream()
                .collect(Collectors.toMap(issue -> issue.ruleKey().rule(), Function.identity(), (rule1, rule2) -> rule1));
    }
}
