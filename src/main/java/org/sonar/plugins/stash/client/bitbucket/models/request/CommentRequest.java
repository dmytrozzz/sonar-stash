package org.sonar.plugins.stash.client.bitbucket.models.request;

import lombok.Builder;

/**
 * Created by dmytro.khaynas on 3/29/17.
 */
public class CommentRequest {
    public static final String FROM_DESTINATION = "FROM";
    public static final String TO_DESTINATION = "TO";

    private final Anchor anchor;
    private final String text;

    @Builder
    private CommentRequest(String text, int line, String type, String fileType, String path, String srcPath) {
        this.text = text;
        this.anchor = new Anchor(line, type, fileType, path, srcPath);
    }

    public class Anchor {

        private Integer line;
        private String lineType;
        private final String fileType;
        private final String path;
        private final String srcPath;

        Anchor(int line, String type, String fileType, String path, String srcPath) {
            if (line != 0) {
                this.line = line;
                this.lineType = type;
            }
            this.fileType = fileType;
            this.path = path;
            this.srcPath = srcPath;
        }
    }
}
