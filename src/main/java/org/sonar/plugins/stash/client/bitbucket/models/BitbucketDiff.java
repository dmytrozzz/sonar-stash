package org.sonar.plugins.stash.client.bitbucket.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

    public String getSrcPath() {
        if (source != null) return source.getToString();
        return "unknown";
    }

    public String getBaseDir() {
        if (destination != null && destination.components != null && destination.components.length > 0)
            return destination.components[0];
        if (source != null && source.components != null && source.components.length > 0)
            return source.components[0];
        return "unknown";
    }

    @Getter
    @EqualsAndHashCode(of = "toString")
    public static class Source {

        private String[] components;
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
    @ToString
    public static class Segment {
        public static final String CONTEXT_TYPE = "CONTEXT";
        public static final String REMOVED_TYPE = "REMOVED";
        public static final String ADDED_TYPE = "ADDED";

        private String type;
        private List<Line> lines;

        public boolean isTypeOfContext() {
            return Objects.equals(CONTEXT_TYPE, type);
        }

        public boolean isNotRemove() {
            return !Objects.equals(REMOVED_TYPE, type);
        }
    }

    @Getter
    @ToString
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
