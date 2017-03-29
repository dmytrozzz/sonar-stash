package org.sonar.plugins.stash.issue.coverage;

import org.sonar.plugins.stash.utils.FormatUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoverageIssue {

    private double uncoveredLines;
    private double linesToCover;

    private double previousCoverage;

    public double getCoverage() {
        double result = 0;
        if ((int) linesToCover != 0) {
            result = FormatUtils.formatDouble((1 - (uncoveredLines / linesToCover)) * 100);
        }

        return result;
    }

    public String getMessage() {
        return "";//Code coverage of file " + path + " lowered from " + previousCoverage + "% to " + getCoverage() + "%.";
    }

    public boolean isLowered() {
        return (previousCoverage - getCoverage()) > 0;
    }

    public String printIssueMarkdown(String sonarQubeURL) {
        StringBuilder sb = new StringBuilder();
        //sb.append(MarkdownPrinter.printSeverityMarkdown(severity)).append(getMessage());

        return sb.toString();
    }

}
