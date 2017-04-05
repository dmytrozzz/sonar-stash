package org.sonar.plugins.stash.client.bitbucket.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BitbucketDiff {

    private Source source;
    private Source destination;
    @Getter
    private List<Hunk> hunks;
    private List<BitbucketComment> lineComments;
    private List<BitbucketComment> fileComments;
    private boolean binary;

    public boolean hasCode() {
        return !binary && hunks != null && !hunks.isEmpty();
    }

    public String getPath() {
        if (destination != null) return destination.getToString();
        if (source != null) return source.getToString();
        return "unknown";
    }

    public String getParent() {
        if (destination != null) return destination.parent;
        if (source != null) return source.parent;
        return "unknown";
    }

    @Getter
    @EqualsAndHashCode(of = "toString")
    public static class Source {

        private String name;
        private String parent;
        private String extension;
        private String toString;
    }

    @Getter
    public static class Hunk {

        private long sourceLine;
        private long sourceSpan;
        private long destinationLine;
        private long destinationSpan;
        private List<Segment> segments;
    }

    @Getter
    public static class Segment {
        public static final String CONTEXT_TYPE = "CONTEXT";
        public static final String REMOVED_TYPE = "REMOVED";
        public static final String ADDED_TYPE = "ADDED";

        private String type;
        private List<Line> lines;

        public boolean isTypeOfContext() {
            return Objects.equals(CONTEXT_TYPE, type);
        }
    }

    @Getter
    public static class Line {

        private int source;
        private int destination;
        private String line;
    }

    @Getter
    public static class BitbucketDiffs {
        private List<BitbucketDiff> diffs;
    }

    @Override
    public int hashCode() {
        if (destination != null) return destination.hashCode();
        if (source != null) return source.hashCode();
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BitbucketDiff && (destination != null && destination.equals(((BitbucketDiff) obj).destination) || source != null && source.equals(((BitbucketDiff) obj).source));
    }

    public Stream<BitbucketComment> getCommentsStream() {
        Stream<BitbucketComment> comments = lineComments != null ? lineComments.stream() : Stream.empty();
        if (fileComments != null)
            return Stream.concat(comments, fileComments.stream());
        return comments;
    }
}
