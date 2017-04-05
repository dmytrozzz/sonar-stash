package org.sonar.plugins.stash.client.bitbucket.models.request;

import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiff;

import java.util.Objects;

/**
 * Created by dmytro.khaynas on 3/29/17.
 */
public class Comment {
    private final Anchor anchor;
    private final String text;

    public Comment(String text, int line, String type, String path) {
        this.text = text;
        this.anchor = new Anchor(line, type, path);
    }

    public class Anchor {

        private int line;
        private String lineType;
        private final String fileType;
        private final String path;

        Anchor(int line, String type, String path) {
            if (line != 0L) {
                this.line = line;
                this.lineType = type;
            }
            this.fileType = Objects.equals(type, BitbucketDiff.Segment.CONTEXT_TYPE) ? "FROM" : "TO";
            this.path = path;
        }
    }
}
