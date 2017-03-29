package org.sonar.plugins.stash.utils;

import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.bootstrap.ProjectBuilder;

import java.io.File;

import lombok.Getter;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class BaseDirProvider extends ProjectBuilder {

    @Getter
    private File projectBaseDir;

    @Override
    public void build(ProjectBuilder.Context context) {
        this.projectBaseDir = context.projectReactor().getRoot().getBaseDir();
    }
}
