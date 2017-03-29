package org.sonar.plugins.stash.client.bitbucket.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.sonar.plugins.stash.config.PullRequestRef;

@Getter
@AllArgsConstructor
public class BitbucketPullRequest {

    @Delegate
    private final PullRequestRef ref;

    @Setter
    private long version;

    private final List<BitbucketUser> reviewers;

    public BitbucketPullRequest(PullRequestRef pr) {
        this.ref = pr;
        this.reviewers = new ArrayList<>();
    }

    public BitbucketUser getReviewer(String user) {
        return reviewers.stream()
                        .filter(stashUser -> Objects.equals(user, stashUser.getSlug()))
                        .findFirst().orElse(null);
    }

    public boolean containsReviewer(BitbucketUser reviewer) {
        return reviewers.stream()
                        .map(BitbucketUser::getId)
                        .anyMatch(id -> id == reviewer.getId());
    }
}
