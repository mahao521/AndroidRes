package less.haku.androidres.request;

import com.squareup.okhttp.Request;

import less.haku.androidres.data.DoubanBook;
import less.haku.androidres.request.base.BaseRequest;

/**
 * Created by HaKu on 15/11/25.
 */
public class DouBanRequest extends BaseRequest {

    //Request的请求参数
    public static class Input {
        public String para1;    //参数1
        public String para2;    //参数2
    }

    public DouBanRequest(String key, int size) {
        url = "https://api.douban.com/v2/book/search?q=" + key + "&start=" + size;
        url = addPublicParam(url);
        request = new Request.Builder()
                .url(url)
                .build();

        outCls = DoubanBook.class;
    }
}
