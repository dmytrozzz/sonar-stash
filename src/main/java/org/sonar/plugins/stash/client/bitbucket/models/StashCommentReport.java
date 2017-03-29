package org.sonar.plugins.stash.client.bitbucket.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@Getter
public class StashCommentReport {

    private static final Logger LOGGER = Loggers.get(StashCommentReport.class);

    @SerializedName("values")
    private List<BitbucketComment> comments = new ArrayList<>();

    private boolean isLastPage;
    private int nextPageStart;

    public void add(BitbucketComment comment) {
        comments.add(comment);
    }

    public void add(StashCommentReport report) {
        for (BitbucketComment comment : report.getComments()) {
            comments.add(comment);
        }
    }

//    public boolean contains(String message, String path, long line) {
//        return comments.stream().anyMatch(comment ->
//                Objects.equals(comment.getText(), message) &&
//                Objects.equals(comment.getPath(), path));
//                        //&& (comment.getLine() == line));
//    }

//    public StashCommentReport applyDiffReport(BitbucketDiffs diffReport) {
//        for (BitbucketComment comment : comments) {
//            BitbucketDiff diff = diffReport.getDiffByComment(comment.getId());
//            if ((diff != null) /*&& diff.isTypeOfContext()*/) {
//
//                // By default comment line, with type == CONTEXT, is set to FROM value.
//                // Set comment line to TO value to be compared with SonarQube issue.
//                //comment.setLine(diff.getDestination());
//
//                LOGGER.debug("Update Stash comment \"{}\": set comment line to destination diff line ()", comment.getId()/*, comment.getLine()*/);
//            }
//        }
//        return this;
//    }

    public int size() {
        return comments.size();
    }

}
