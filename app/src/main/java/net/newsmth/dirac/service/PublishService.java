package net.newsmth.dirac.service;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PublishService {

    /**
     * <p>Possible responses:</p>
     * <pre>
     * {
     * "ajax_st": 0,
     * "ajax_code": "0204",
     * "ajax_msg": "您无权在本版发表文章"
     * }
     * </pre>
     * <pre>
     * {
     * "ajax_code": "0406",
     * "list": [{
     * "text": "版面:测试专用版面(Test)",
     * "url": "/board/Test"
     * }, {
     * "text": "主题:test",
     * "url": "/article/Test/916000"
     * }, {
     * "text": "水木社区",
     * "url": "/mainpage"
     * }],
     * "default": "/board/Test",
     * "ajax_st": 1,
     * "ajax_msg": "发表成功"
     * }
     * </pre>
     */
    @POST("nForum/article/{board}/ajax_post.json")
    @FormUrlEncoded
    @Headers("X-Requested-With: XMLHttpRequest")
    Observable<String> publish(@Path("board") String board,
                               @Field("subject") String subject,
                               @Field("content") String body,
                               @Field("id") String id);

}
