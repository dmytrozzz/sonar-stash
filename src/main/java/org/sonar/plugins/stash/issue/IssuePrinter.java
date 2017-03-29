package org.sonar.plugins.stash.issue;

import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;

/**
 * Created by dmytro.khaynas on 3/31/17.
 */
public class IssuePrinter {
    static final String CODING_RULES_RULE_KEY = "coding_rules#rule_key=";
    static final String ISSUE_LINK = "issues#issues=";

    public static String printIssueMarkdown(String sonarQubeURL, BitbucketIssue issue) {
        return printSeverityMarkdown(issue.severity()) + issue.message() +
                " [" + printIssueLink(issue, sonarQubeURL) + " | " + printRuleLink(issue, sonarQubeURL) + "]";
    }

    public static String printIssueTask(BitbucketIssue issue) {
        return issue.message();
    }

    private static String printIssueLink(BitbucketIssue issue, String sonarQubeURL) {
        return printLink("Issue in Sonar", sonarQubeURL + "/" + ISSUE_LINK + issue.getKey());
    }

    private static String printRuleLink(BitbucketIssue issue, String sonarQubeURL) {
        return printLink("About rule", sonarQubeURL + "/" + CODING_RULES_RULE_KEY + issue.ruleKey());
    }

    private static String printLink(String title, String url) {
        return String.format("[%s](%s)", title, url);
    }

    private static String printSeverityMarkdown(Severity severity) {
        return "*" + severity.name() + "*" + " - ";
    }
}
