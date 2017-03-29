package org.sonar.plugins.stash.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PullRequestRef {

    private final String project;
    private final String repository;
    private final int id;
}
