package org.sonar.plugins.stash.config;

import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Settings;
import org.sonar.plugins.stash.BitbucketPlugin;
import org.sonar.plugins.stash.exceptions.StashConfigurationException;

import java.text.MessageFormat;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
@ScannerSide
public class StashPluginConfiguration {

    private static final String EXCEPTION_STASH_CONF = "Unable to get {0} from plugin configuration (value is empty)";

    private Settings settings;

    public StashPluginConfiguration(Settings settings) {
        this.settings = settings;
    }

    public PullRequestRef getPullRequest() throws StashConfigurationException {
        return PullRequestRef.builder()
                             .project(getStashProject())
                             .repository(getStashRepository())
                             .id(getStashPullRequestId())
                             .build();
    }

    /**
     * Mandatory Stash Project option.
     *
     * @throws StashConfigurationException if unable to get parameter
     */
    private String getStashProject() throws StashConfigurationException {
        String result = getStashProjectConfig();
        if (result == null) {
            throw new StashConfigurationException(MessageFormat.format(EXCEPTION_STASH_CONF, BitbucketPlugin.STASH_PROJECT));
        }
        return result;
    }

    /**
     * Mandatory Stash Repository option.
     *
     * @throws StashConfigurationException if unable to get parameter
     */
    private String getStashRepository() throws StashConfigurationException {
        String result = getStashRepositoryConfig();
        if (result == null) {
            throw new StashConfigurationException(MessageFormat.format(EXCEPTION_STASH_CONF, BitbucketPlugin.STASH_REPOSITORY));
        }

        return result;
    }

    /**
     * Mandatory Stash pull-request ID option.
     *
     * @throws StashConfigurationException if unable to get parameter
     */
    private int getStashPullRequestId() throws StashConfigurationException {
        Integer result = getPullRequestId();
        if (result == 0) {
            throw new StashConfigurationException(MessageFormat.format(EXCEPTION_STASH_CONF, BitbucketPlugin.STASH_PULL_REQUEST_ID));
        }
        return result;
    }

    /**
     * Mandatory Stash URL option.
     *
     * @throws StashConfigurationException if unable to get parameter
     */
    public String getStashURL() throws StashConfigurationException {
        String result = getStashURLConfig();
        if (result == null) {
            throw new StashConfigurationException(MessageFormat.format(EXCEPTION_STASH_CONF, BitbucketPlugin.STASH_URL));
        }

        if (result.endsWith("/")) {
            //LOGGER.warn("Stripping trailing slash from {}, as it leads to invalid URLs", BitbucketPlugin.STASH_URL);
            result = result.substring(0, result.length()-1);
        }

        return result;
    }

    public StashCredentials getCredentials() throws StashConfigurationException {
        String passwordEnvVariable = getStashPasswordEnvironmentVariable();
        String password = getStashPassword();
        if (passwordEnvVariable != null) {
            password = System.getenv(passwordEnvVariable);
            if (password == null) {
                throw new StashConfigurationException(
                        "Unable to retrieve password from configured environment variable " +
                                BitbucketPlugin.STASH_PASSWORD_ENVIRONMENT_VARIABLE);
            }
        }
        return new StashCredentials(getStashLogin(), password);
    }

    /**
     * Mandatory Issue Threshold option.
     *
     * @throws StashConfigurationException if unable to get parameter as Integer
     */
    public int getIssueThreshold() throws StashConfigurationException {
        int result = 0;
        try {
            result = getIssueThresholdConfig();
        } catch (NumberFormatException e) {
            throw new StashConfigurationException("Unable to get " + BitbucketPlugin.STASH_ISSUE_THRESHOLD + " from plugin configuration", e);
        }
        return result;
    }

    public boolean hasToNotifyStash() {
        return settings.getBoolean(BitbucketPlugin.STASH_ENABLED);
    }

    private String getStashProjectConfig() {
        return settings.getString(BitbucketPlugin.STASH_PROJECT);
    }

    private String getStashRepositoryConfig() {
        return settings.getString(BitbucketPlugin.STASH_REPOSITORY);
    }

    private Integer getPullRequestId() {
        return settings.getInt(BitbucketPlugin.STASH_PULL_REQUEST_ID);
    }

    private String getStashURLConfig() {
        return settings.getString(BitbucketPlugin.STASH_URL);
    }

    private String getStashLogin() {
        return settings.getString(BitbucketPlugin.STASH_LOGIN);
    }

    private String getStashPassword() {
        return settings.getString(BitbucketPlugin.STASH_PASSWORD);
    }

    private String getStashPasswordEnvironmentVariable() {
        return settings.getString(BitbucketPlugin.STASH_PASSWORD_ENVIRONMENT_VARIABLE);
    }

    public String getSonarQubeURL() {
        return settings.hasKey(BitbucketPlugin.SONARQUBE_NICE_URL) ?
                settings.getString(BitbucketPlugin.SONARQUBE_NICE_URL) : settings.getString(BitbucketPlugin.SONARQUBE_LEGACY_URL);
    }

    private int getIssueThresholdConfig() {
        return settings.getInt(BitbucketPlugin.STASH_ISSUE_THRESHOLD);
    }

    public int getStashTimeout() {
        return settings.getInt(BitbucketPlugin.STASH_TIMEOUT);
    }

    public boolean canApprovePullRequest() {
        return settings.getBoolean(BitbucketPlugin.STASH_REVIEWER_APPROVAL);
    }

    public boolean resetComments() {
        return settings.getBoolean(BitbucketPlugin.STASH_RESET_COMMENTS);
    }

    public String getCodeCoverageSeverity() {
        return settings.getString(BitbucketPlugin.STASH_CODE_COVERAGE_SEVERITY);
    }

    public String getTaskIssueSeverityThreshold() {
        return settings.getString(BitbucketPlugin.STASH_TASK_SEVERITY_THRESHOLD);
    }

//    public String getSonarQubeVersion() {
//        return settings.getString(CoreProperties.SERVER_VERSION);
//    }

    public boolean includeAnalysisOverview() {
        return settings.getBoolean(BitbucketPlugin.STASH_INCLUDE_ANALYSIS_OVERVIEW);
    }
}