package org.sonar.plugins.stash;

import org.sonar.api.*;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rule.Severity;
import org.sonar.plugins.stash.config.StashPluginConfiguration;
import org.sonar.plugins.stash.utils.BaseDirProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Properties({
        @Property(key = BitbucketPlugin.STASH_ENABLED, name = "Stash Notification", defaultValue = "false", description = "Analysis result will be issued in Stash pull request", global = false),
        @Property(key = BitbucketPlugin.STASH_PROJECT, name = "Stash Project", description = "Stash project of current pull-request", global = false),
        @Property(key = BitbucketPlugin.STASH_REPOSITORY, name = "Stash Repository", description = "Stash project of current pull-request", global =
                false),
        @Property(key = BitbucketPlugin.STASH_PULL_REQUEST_ID, name = "Stash Pull-request Id", description = "Stash pull-request Id", global = false)})
public class BitbucketPlugin implements Plugin {

    private static final String DEFAULT_STASH_TIMEOUT_VALUE = "10";
    private static final String DEFAULT_STASH_THRESHOLD_VALUE = "100";
    private static final boolean DEFAULT_STASH_ANALYSIS_OVERVIEW = true;
    private static final boolean DEFAULT_STASH_INCLUDE_EXISTING_ISSUES = false;

    private static final String CONFIG_PAGE_SUB_CATEGORY_STASH = "Stash";

    public static final String SEVERITY_NONE = "NONE";

    // INFO, MINOR, MAJOR, CRITICAL, BLOCKER
    public static final List<String> SEVERITY_LIST = Severity.ALL;

    public static final String STASH_ENABLED = "sonar.stash.enabled";
    public static final String STASH_PROJECT = "sonar.stash.project";
    public static final String STASH_REPOSITORY = "sonar.stash.repository";
    public static final String STASH_PULL_REQUEST_ID = "sonar.stash.pullrequest.id";
    public static final String STASH_RESET_COMMENTS = "sonar.stash.comments.reset";
    public static final String STASH_URL = "sonar.stash.url";
    public static final String STASH_LOGIN = "sonar.stash.login";
    public static final String STASH_PASSWORD = "sonar.stash.password";
    public static final String STASH_PASSWORD_ENVIRONMENT_VARIABLE = "sonar.stash.password.variable";
    public static final String STASH_REVIEWER_APPROVAL = "sonar.stash.reviewer.approval";
    public static final String STASH_ISSUE_THRESHOLD = "sonar.stash.issue.threshold";
    public static final String STASH_TIMEOUT = "sonar.stash.timeout";
    public static final String SONARQUBE_NICE_URL = "sonar.core.serverBaseURL";
    public static final String SONARQUBE_LEGACY_URL = "sonar.host.url";
    public static final String STASH_TASK_SEVERITY_THRESHOLD = "sonar.stash.task.issue.severity.threshold";
    public static final String STASH_CODE_COVERAGE_SEVERITY = "sonar.stash.coverage.severity.threshold";
    public static final String STASH_INCLUDE_ANALYSIS_OVERVIEW = "sonar.stash.include.overview";

    private List getExtensions() {
        return Arrays.asList(
                BitbucketReportingPostJob.class,
                StashPluginConfiguration.class,
                BaseDirProvider.class,
                PropertyDefinition.builder(STASH_URL)
                        .name("Stash base URL")
                        .description("HTTP URL of Stash instance, such as http://yourhost.yourdomain/stash")
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .onQualifiers(Qualifiers.PROJECT).build(),
                PropertyDefinition.builder(STASH_LOGIN)
                        .name("Stash base User")
                        .description("User to push data on Stash instance")
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .onQualifiers(Qualifiers.PROJECT).build(),
                PropertyDefinition.builder(STASH_TIMEOUT)
                                  .name("Stash issue Timeout")
                                  .description("Timeout when pushing a new issue to Stash (in sec)")
                                  .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                                  .onQualifiers(Qualifiers.PROJECT)
                                  .defaultValue(DEFAULT_STASH_TIMEOUT_VALUE).build(),
                PropertyDefinition.builder(STASH_REVIEWER_APPROVAL)
                        .name("Stash reviewer approval")
                        .description("Does SonarQube approve the pull-request if there is no new issues?")
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .onQualifiers(Qualifiers.PROJECT)
                        .type(PropertyType.BOOLEAN)
                        .defaultValue("false").build(),
                PropertyDefinition.builder(STASH_ISSUE_THRESHOLD)
                        .name("Stash issue Threshold")
                        .description("Threshold to limit the number of issues pushed to Stash server")
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .onQualifiers(Qualifiers.PROJECT)
                        .defaultValue(DEFAULT_STASH_THRESHOLD_VALUE).build(),
                PropertyDefinition.builder(STASH_CODE_COVERAGE_SEVERITY)
                        .name("Stash code coverage severity")
                        .description("Severity to be associated with Code Coverage issues")
                        .type(PropertyType.SINGLE_SELECT_LIST)
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .options(Stream.concat(SEVERITY_LIST.stream(), Stream.of(SEVERITY_NONE)).collect(Collectors.toList())).build(),
                PropertyDefinition.builder(STASH_TASK_SEVERITY_THRESHOLD)
                        .name("Stash tasks severity threshold")
                        .description("Only create tasks for issues with the same or higher severity")
                        .type(PropertyType.SINGLE_SELECT_LIST)
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .onQualifiers(Qualifiers.PROJECT)
                        .defaultValue(SEVERITY_NONE)
                        .options(Stream.concat(SEVERITY_LIST.stream(), Stream.of(SEVERITY_NONE)).collect(Collectors.toList())).build(),
                PropertyDefinition.builder(STASH_INCLUDE_ANALYSIS_OVERVIEW)
                        .name("Include Analysis Overview Comment")
                        .description("Create a comment to  the Pull Request providing a overview of the results")
                        .type(PropertyType.BOOLEAN)
                        .subCategory(CONFIG_PAGE_SUB_CATEGORY_STASH)
                        .onQualifiers(Qualifiers.PROJECT)
                        .defaultValue(Boolean.toString(false)).build()
        );
    }

    @Override
    public void define(Context context) {
        context.addExtensions(getExtensions());
    }
}

