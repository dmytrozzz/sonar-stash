package org.sonar.plugins.stash.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StashCredentials {

    private final String login;
    private final String password;
}
