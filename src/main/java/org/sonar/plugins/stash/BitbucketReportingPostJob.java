package org.sonar.plugins.stash;

import lombok.AllArgsConstructor;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.config.StashPluginConfiguration;
import org.sonar.plugins.stash.exceptions.StashConfigurationException;
import org.sonar.plugins.stash.service.ReportService;
import org.sonar.plugins.stash.utils.BaseDirProvider;

import static org.sonar.plugins.stash.service.ReportService.STACK_TRACE;

@AllArgsConstructor
public class BitbucketReportingPostJob implements PostJob {

    private static final Logger LOGGER = Loggers.get(BitbucketReportingPostJob.class);

    private final StashPluginConfiguration config;
    private final ProjectIssues projectIssues;
    private final BaseDirProvider baseDirProvider;

    @Override
    public void describe(PostJobDescriptor descriptor) {
        descriptor.name(getClass().getSimpleName());
    }

    @Override
    public void execute(PostJobContext context) {
        LOGGER.info("Start bitbucket reporting.");
        try {
            if (config.hasToNotifyStash()) {
                LOGGER.info("Plugin reporting enabled.");
                ReportService.create(config, projectIssues, baseDirProvider.getProjectBaseDir())
                        .reportFromSonarToBitbucket(context);
            }
        } catch (StashConfigurationException e) {
            LOGGER.error("Unable to push SonarQube service to Stash: {}", e.getMessage());
            LOGGER.debug(STACK_TRACE, e);
        }
    }
}
