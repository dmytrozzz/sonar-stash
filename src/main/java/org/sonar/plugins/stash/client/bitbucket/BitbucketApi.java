package org.sonar.plugins.stash.client.bitbucket;

import org.sonar.plugins.stash.client.bitbucket.models.BitbucketTask;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketUser;
import org.sonar.plugins.stash.client.bitbucket.models.request.CommentTaskRequest;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

/**
 * Created by dmytro.khaynas on 3/30/17.
 */
public interface BitbucketApi {

    @POST("tasks")
    Call<BitbucketTask> postTaskOnComment(@Body CommentTaskRequest commentTaskRequest);

    @PUT("tasks/{taskId}")
    Call<Void> editTask(@Path("taskId") long taskId, @Body Map<String, String> commentTask);

    @DELETE("tasks/{taskId}")
    Call<Void> deleteTask(@Path("taskId") long taskId);

    @GET("users/{slug}")
    Call<BitbucketUser> getUser(@Path("slug") String userSlug);
}
