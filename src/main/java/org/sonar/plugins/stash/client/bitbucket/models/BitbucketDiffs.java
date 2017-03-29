package org.sonar.plugins.stash.client.bitbucket.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a representation of the Stash Diff view.
 * <p>
 * Purpose is to check if a SonarQube issue belongs to the Stash diff view before posting.
 * Indeed, Stash Diff view displays only comments which belong to this view.
 */
@Getter
public class BitbucketDiffs {
    private List<BitbucketDiff> diffs = new ArrayList<>();
}
