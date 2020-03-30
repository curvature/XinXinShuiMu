package net.newsmth.dirac.service;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("https://tooling.fun/qqwry")
    Observable<ResponseBody> getIpData();

    @POST("nForum/user/ajax_login.json")
    @FormUrlEncoded
    @Headers("X-Requested-With: XMLHttpRequest")
    Observable<String> login(@Field("id") String username, @Field("passwd") String password);

    @GET("nForum/user/query/{username}.json")
    @Headers("X-Requested-With: XMLHttpRequest")
    Observable<String> getUserInfo(@Path("username") String username);

    @GET("nForum/user/ajax_session.json")
    @Headers("X-Requested-With: XMLHttpRequest")
    Observable<String> getStatus();

    @GET("nForum/board/{board}")
    Observable<ResponseBody> getThreadSummary(@Path("board") String board, @Query("p") int page);

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/s/article?ajax")
    Observable<ResponseBody> searchArticle(
            @Query("t1") String keyword,
            @Query("au") String author,
            @Query("m") String gilded,
            @Query("a") String attachment,
            @Query("b") String boardEnglish,
            @Query("p") int page);

    @GET("nForum/mainpage")
    Observable<ResponseBody> getMainPage();

    @GET("mainpage.html")
    Observable<ResponseBody> getMainPage2();

    @GET("nForum/section/{section}")
    Observable<ResponseBody> getSection(@Path("section") String section);

    @GET("nForum/article/{board}/{postId}")
    Observable<ResponseBody> getThread(@Path("board") String board, @Path("postId") String postId, @Query("p") int page);

    @POST("nForum/article/{board}/ajax_delete/{id}.json")
    @Headers("X-Requested-With: XMLHttpRequest")
    Observable<ResponseBody> deletePost(@Path("board") String board, @Path("id") String id);
}
