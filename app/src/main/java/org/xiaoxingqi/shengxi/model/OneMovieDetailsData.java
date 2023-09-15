package org.xiaoxingqi.shengxi.model;

public class OneMovieDetailsData extends BaseRepData {


    /**
     * data : {"id":155,"name":"黄金兄弟","description":{"movie_type":"剧情,动作,犯罪","country":"中国大陆","duration":"100分钟","relase_info":"2018-09-21 08:00大陆上映","summary":"狮王（郑伊健 饰）、火山（陈小春 饰）、Bill（谢天华 饰）、淡定（钱嘉乐 饰）、老鼠（林晓峰 饰）五个出生入死的兄弟，在恩师曹sir（曾志伟 饰）的带领下，为了救济儿童而偷取特效药，却惨遭设局，陷入枪林弹雨的险境之中。兄弟们抱着视死如归的豪情，展开一连串的追查与激战。他们明白，即使无法活着回来，也比一人活着痛快！","directors":["钱嘉乐"],"actors":[{"name":"郑伊健","role":"饰：狮王"},{"name":"陈小春","role":"饰：火山"},{"name":"谢天华","role":"饰：Bill"},{"name":"钱嘉乐","role":"饰：淡定"}]},"img":"http://p0.meituan.net/movie/04e1894c8ce14ad9214fd467cbb2cdd11031752.jpg@464w_644h_1e_1c","score":"0","question":{"id":1,"name":"rate_movie","type":"multiple_choice","description":null,"answer_limit":1,"createdAt":"2018-09-17T07:58:00.000Z","updatedAt":"2018-09-17T07:58:00.000Z","Options":[{"id":2,"name":"bad","value":"0","question_id":1,"createdAt":"2018-09-17T08:01:32.000Z","updatedAt":"2018-09-17T08:01:32.000Z"},{"id":3,"name":"fair","value":"5","question_id":1,"createdAt":"2018-09-17T08:01:44.000Z","updatedAt":"2018-09-17T08:01:44.000Z"},{"id":4,"name":"good","value":"10","question_id":1,"createdAt":"2018-09-17T08:01:52.000Z","updatedAt":"2018-09-17T08:01:52.000Z"}]},"options":[{"id":2,"name":"bad","value":"0","question_id":1,"createdAt":"2018-09-17T08:01:32.000Z","updatedAt":"2018-09-17T08:01:32.000Z"},{"id":3,"name":"fair","value":"5","question_id":1,"createdAt":"2018-09-17T08:01:44.000Z","updatedAt":"2018-09-17T08:01:44.000Z"},{"id":4,"name":"good","value":"10","question_id":1,"createdAt":"2018-09-17T08:01:52.000Z","updatedAt":"2018-09-17T08:01:52.000Z"}]}
     */

    private MovieData.MovieBean data;

    public MovieData.MovieBean getData() {
        return data;
    }

    public void setData(MovieData.MovieBean data) {
        this.data = data;
    }


}
