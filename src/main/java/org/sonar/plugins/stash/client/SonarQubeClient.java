package org.sonar.plugins.stash.client;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.stash.client.sonar.models.SonarCoverage;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

@Getter
public class SonarQubeClient {

    private static final Logger LOGGER = Loggers.get(SonarQubeClient.class);

    private final String baseUrl;
    private final SonarApi sonarApi;

    public SonarQubeClient(String baseUrl) {
        this.baseUrl = baseUrl;
        sonarApi = ClientFactory.buildRetrofit(baseUrl, 10).build().create(SonarApi.class);
    }

    public double getCoveragePerProject(String projectKey) {
        return getCoverage(projectKey);
    }

    public double getCoveragePerFile(String projectKey, String filePath) {
        return getCoverage(projectKey + ":" + filePath);
    }

    private double getCoverage(String key) {
        try {
            Response<List<SonarCoverage>> response = getSonarApi().getCoverage(key).execute();
            if (!response.isSuccessful()) {
                LOGGER.debug("Unable to get the coverage on resource " + key + ": " + response.errorBody().string() + "(" + response.code() + ")");
            } else {
                List<SonarCoverage> coverage = response.body();
                return coverage.size() > 0 ? coverage.get(0).getCoverage() : 0;
            }
        } catch (IOException e) {
            LOGGER.error("Unable to get the coverage on resource " + key, e);
        }
        return 0;
    }

    public interface SonarApi {

        @GET("/api/resources?metrics=line_coverage&format=json")
        Call<List<SonarCoverage>> getCoverage(@Query("resource") String resource);
    }
}
