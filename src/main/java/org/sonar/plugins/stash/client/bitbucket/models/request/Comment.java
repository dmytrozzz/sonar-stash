package org.sonar.plugins.stash.client.bitbucket.models.request;

import org.sonar.plugins.stash.BitbucketPlugin;

import java.util.Objects;

/**
 * Created by dmytro.khaynas on 3/29/17.
 */
public class Comment {

    public static final String CONTEXT_ISSUE_TYPE = "CONTEXT";
    public static final String REMOVED_ISSUE_TYPE = "REMOVED";
    public static final String ADDED_ISSUE_TYPE = "ADDED";

    private final Anchor anchor;
    private final String text;

    public Comment(String text, long line, String type, String path) {
        this.text = text;
        this.anchor = new Anchor(line, type, path);
    }

    public class Anchor {

        private long line;
        private String lineType;
        private final String fileType;
        private final String path;

        public Anchor(long line, String type, String path) {
            if (line != 0L) {
                this.line = line;
                this.lineType = type;
            }
            this.fileType = Objects.equals(type, CONTEXT_ISSUE_TYPE) ? "FROM" : "TO";
            this.path = path;
        }
    }
}
