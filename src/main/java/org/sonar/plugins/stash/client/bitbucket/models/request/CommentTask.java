package org.sonar.plugins.stash.client.bitbucket.models.request;

import lombok.Getter;
/**
 * Created by dmytro.khaynas on 3/29/17.
 */
@Getter
public class CommentTask {

    private final CommentAnchor anchor;
    private final String text;

    @Getter
    public static class CommentAnchor {

        private final long id;
        private final String type = "COMMENT";

        public CommentAnchor(long id) {
            this.id = id;
        }
    }

    public CommentTask(long id, String message) {
        anchor = new CommentAnchor(id);
        this.text = message;
    }
}
