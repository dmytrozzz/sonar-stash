package org.sonar.plugins.stash.client.bitbucket.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class BitbucketUser {

    private final long id;
    private final String name;
    private final String slug;
    private final String emailAddress;
    private final String displayName;

    public String getEmail(){
        return emailAddress;
    }
}
