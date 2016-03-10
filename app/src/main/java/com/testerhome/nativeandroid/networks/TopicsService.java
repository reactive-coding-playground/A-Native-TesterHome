package com.testerhome.nativeandroid.networks;

import com.testerhome.nativeandroid.models.CollectTopicResonse;
import com.testerhome.nativeandroid.models.CreateReplyResponse;
import com.testerhome.nativeandroid.models.NotificationResponse;
import com.testerhome.nativeandroid.models.PraiseEntity;
import com.testerhome.nativeandroid.models.SearchResponse;
import com.testerhome.nativeandroid.models.TopicDetailResponse;
import com.testerhome.nativeandroid.models.TopicReplyResponse;
import com.testerhome.nativeandroid.models.TopicsResponse;
import com.testerhome.nativeandroid.models.ToutiaoResponse;
import com.testerhome.nativeandroid.models.UserDetailResponse;
import com.testerhome.nativeandroid.models.UserResponse;

import retrofit.Call;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Bin Li on 2015/9/15.
 */
public interface TopicsService {

    @GET("ads/toutiao.json")
    Observable<ToutiaoResponse> getToutiao();


    @GET("topics.json")
    Observable<TopicsResponse> getTopicsByType(@Query("type") String type,
                                               @Query("offset") int offset);

    @GET("search.json")
    Call<SearchResponse> searchTopicsByKeyword(@Query("q") String keyword,
                                         @Query("page") int page);


    @GET("topics.json")
    Observable<TopicsResponse> getTopicsByNodeId(@Query("node_id") int nodeId,
                                           @Query("offset") int offset
    );


    @GET("topics/{id}.json")
    Observable<TopicDetailResponse> getTopicById(@Path("id") String id
    );


    @GET("users/{username}/topics.json")
    Observable<TopicsResponse> getUserTopics(@Path("username") String username,
                                       @Query("access_token") String accessToken,
                                       @Query("offset") int offset
    );

    @GET("users/{username}.json")
    Observable<UserResponse> getUserInfo(
            @Path("username") String username
    );

    @GET("greet.json")
    Observable<UserDetailResponse> getCurrentUserInfo(
            @Query("access_token") String accessToken
    );

    @GET("topics/{id}/replies.json")
    Observable<TopicReplyResponse> getTopicsReplies(@Path("id") String id,
                                              @Query("offset") int offset
    );

    @GET("notifications.json")
    Observable<NotificationResponse> getNotifications(@Query("access_token") String access_token,
                                                @Query("offset") int offset
    );

    @POST("topics/{id}/replies.json")
    Observable<CreateReplyResponse> createReply(@Path("id") String id, @Query("body")   String body,
                                          @Query("access_token") String accessToken
    );

    @POST("topics/{id}/favorite.json")
    Observable<CollectTopicResonse> collectTopic(@Path("id") String id,
                                           @Query("access_token") String accessToken
    );

    @GET("users/{username}/favorites.json")
    Observable<TopicsResponse> getUserFavorite(@Path("username") String username,
                                         @Query("access_token") String accessToken,
                                         @Query("offset") int offset
    );

    @POST("topics/{id}/unfavorite.json")
    Observable<CollectTopicResonse> uncollectTopic(@Path("id") String id,
                                             @Query("access_token") String accessToken
    );

    @POST("likes.json")
    Observable<PraiseEntity> praiseTopic(@Query("obj_type") String objType,
                                   @Query("obj_id") String objId,
                                   @Query("access_token") String accessToken
    );

    @DELETE("likes.json")
    Observable<PraiseEntity> unLikeTopic(@Query("obj_type") String objType,
                                   @Query("obj_id") String objId,
                                   @Query("access_token") String accessToken);
}
