package org.sonar.plugins.stash.client.bitbucket.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode(of = {"id"})
public class BitbucketComment {

    private long id;
    private int version;
    private String text;
    private BitbucketUser author;
    private long createdDate;
    private long updatedDate;
    private List<BitbucketComment> comments;
    private List<BitbucketTask> tasks;

    public boolean isDeleteable() {
        return tasks.stream().allMatch(BitbucketTask::isDeletable);
    }
}