package org.sonar.plugins.stash.issue.coverage;

//import org.sonar.plugins.stash.issue.Report;
//import org.sonar.plugins.stash.utils.FormatUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


//public class CoverageIssuesReport extends Report {

 //   private double previousProjectCoverage;

//    public CoverageIssuesReport() {
//        super();
//        this.previousProjectCoverage = 0;
//    }
//
//    public double getPreviousProjectCoverage() {
//        return previousProjectCoverage;
//    }
//
//    public void setPreviousProjectCoverage(double previousProjectCoverage) {
//        this.previousProjectCoverage = previousProjectCoverage;
//    }
//
//    public boolean isEmpty() {
//        return ((int) getProjectCoverage() == 0) && getIssues().isEmpty();
//    }
//
//    public List<CoverageIssue> getLoweredIssues() {
//        return issues.stream()
//                //.filter(CoverageIssue::isLowered)
//                .collect(Collectors.toList());
//    }
//
//    public int countLoweredIssues(final String severity) {
//        return (int) getLoweredIssues().stream()
//                //.filter(issue -> Objects.equals(issue.getSeverity(), severity))
//                .count();
//    }
//
//    public int countLoweredIssues() {
//        return getLoweredIssues().size();
//    }
//
//    public double getProjectCoverage() {
//        double result = 0;
//
//        double sumLinesToCover = 0;
//        double sumUncoveredLines = 0;
//
//        for (Issue issue : issues) {
//            sumLinesToCover += ((CoverageIssue) issue).getLinesToCover();
//            sumUncoveredLines += ((CoverageIssue) issue).getUncoveredLines();
//        }
//
//        if ((int) sumLinesToCover != 0) {
//            result = FormatUtils.formatDouble((1 - (sumUncoveredLines / sumLinesToCover)) * 100);
//        }
//
//        return result;
//    }
//
//    public double getEvolution() {
//        double diffProjectCoverage = this.getProjectCoverage() - this.getPreviousProjectCoverage();
//        return FormatUtils.formatDouble(diffProjectCoverage);
//    }
//}
