package org.sonar.plugins.stash.client.bitbucket;

import org.sonar.plugins.stash.client.bitbucket.models.BitbucketComment;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiff;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketPullRequest;
import org.sonar.plugins.stash.client.bitbucket.models.StashCommentReport;
import org.sonar.plugins.stash.client.bitbucket.models.request.CommentRequest;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by dmytro.khaynas on 3/30/17.
 */
public interface BitbucketPrApi {

    @GET("./")
    Call<BitbucketPullRequest> getPR();

    @PUT("./")
    Call<BitbucketPullRequest> editPR(@Body BitbucketPullRequest pullRequest);

    @POST("approve")
    Call<Void> approvePR();

    @DELETE("approve")
    Call<Void> resetApproveOnPR();

    @GET("comments")
    Call<StashCommentReport> getPRComments(@Query("path") String path, @Query("start") long start);

    @POST("comments")
    Call<ResponseBody> postCommentOnPR(@Body Map<String, String> body);

    @POST("comments")
    Call<BitbucketComment> postCommentOnLine(@Body CommentRequest commentRequest);

    @DELETE("comments/{commentId}")
    Call<Void> deleteCommentFromPR(@Path("commentId") long commentId, @Query("version") long versionId);

    @GET("diff?withComments=true")
    Call<BitbucketDiff.BitbucketDiffs> getPRDiffs();
}
