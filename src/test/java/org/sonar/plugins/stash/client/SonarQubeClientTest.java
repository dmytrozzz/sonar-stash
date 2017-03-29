package org.sonar.plugins.stash.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.plugins.stash.client.sonar.models.SonarCoverage;

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SonarQubeClientTest {

    private static final String SONARQUBE_URL = "http://sonar";
    private static final String PROJECT_KEY = "projectKey";

    @Mock
    SonarQubeClient myClient;

    MockWebServer mockWebServer;

    SonarQubeClient.SonarApi sonarApi;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockWebServer.url(SONARQUBE_URL).toString())
                //TODO Add your Retrofit parameters here
                .build();
        sonarApi = retrofit.create(SonarQubeClient.SonarApi.class);

        MockitoAnnotations.initMocks(this);
        doReturn(sonarApi).when(myClient).getSonarApi();
    }

    @After
    public void after() throws IOException {
    }

    @Test
    public void testGetCoveragePerProject() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"msr\": [{\"key\":\"line_coverage\", \"val\": 60.0}]}]"));

        List<SonarCoverage> coverageList = sonarApi.getCoverage(PROJECT_KEY).execute().body();
        System.err.println(coverageList);
        double result = myClient.getCoveragePerProject(PROJECT_KEY);

        Assert.assertSame(60.0, result);
        verify(sonarApi, times(1));
    }

//    @Test
//    public void testGetCoveragePerProjectWithUnknownKey() throws Exception {
//        double result = myClient.getCoveragePerProject(PROJECT_KEY);
//
//        assertTrue(result == 0);
//        verify(sonarApi, times(1));
//    }

    @Test
    public void testGetCoveragePerFile() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"msr\": [{\"key\":\"line_coverage\", \"val\": 60.0}]}]"));

        double result = myClient.getCoveragePerFile(PROJECT_KEY, "file1");

        assertSame(60.0, result);
        verify(sonarApi, times(1));
    }

//    @Test
//    public void testGetCoveragePerFileWithUnknownFile() throws Exception {
//        double result = myClient.getCoveragePerFile(PROJECT_KEY, "file1");
//
//        assertTrue(result == 0);
//        verify(sonarApi, times(1));
//    }
}
