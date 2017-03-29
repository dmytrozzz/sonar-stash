package org.sonar.plugins.stash.client.bitbucket.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class BitbucketTask {
    public static final String OPEN = "OPEN";
    public static final String RESOLVED = "RESOLVED";

    private final Long id;
    private final String text;
    private final String state;
    private final BitbucketUser author;
    @Delegate
    private final PermittedOperations permittedOperations;

    public boolean isOpen() {
        return Objects.equals(OPEN, state);
    }

    @Getter
    public static class PermittedOperations {
        private boolean deletable;
        private boolean editable;
    }
}
