package less.haku.androidres;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import less.haku.androidres.application.HApplication;
import less.haku.androidres.common.BaseActivity;
import less.haku.androidres.core.activity.HomeActivity;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by HaKu on 16/1/11.
 * App Splash页
 */
public class SplashActivity extends BaseActivity {

    @Bind(R.id.splash_time)
    TextView splashTime;    //显示等待剩余时间
    @Bind(R.id.splash_icon)
    ImageView splashIcon;
    @Bind(R.id.splash_debug_info)
    TextView splashDebugInfo;

    private static final int TIME_SECOND = 1;  //标识1s

    private Long showTime;   //闪页显示时间


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        showTime = 3L;   //总等待时间，使用大写L，便于与数字1区分
        //初始化UI
        init();
        //进入APP倒计时
        startTiming();
    }

    /**
     * 测试环境下，显示编译号
     */
    private void initDebugVersion() {
        try {
            ApplicationInfo appInfo = HApplication.instance().getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);

            String version = appInfo.metaData.getString("DEBUG_VERSION");
            splashDebugInfo.setText(version);
        } catch (Exception e) {
            Log.e("debug info","获取Debug版本号失败");
        }

    }

    /**
     * 初始化UI
     */
    public void init() {
        initDebugVersion();
        splashTime.setText(getString(R.string.splash_time, showTime));
    }

    /**
     * 立即进入APP，无视等待时间，跳过Splash页
     */
    @OnClick(R.id.splash_time)
    public void enterAppImmediately() {
        enterApp(showTime);
    }

    /**
     * 进入APP倒计时
     * 使用RxJava进行异步处理
     * 每秒调用一次enterMain
     */
    public void startTiming() {

        //订阅事件，每秒判断一次是否进入APP，并刷新UI展示
        Subscription subscription = Observable.interval(TIME_SECOND, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //aLong代表interval调用次数
                        enterApp(aLong);
                        Log.d("test leak", aLong + "");
                    }
                });

        //将 订阅事件 加入 subscription集合(Set)，用于与Activity生命周期绑定，onDestroy时解除事件注册
        compositeSubscription.add(subscription);
    }

    /**
     * 判断时间是否达到，达到则进入APP
     *
     * @param along 事件调用次数，周期1s，即已过时间
     */
    public void enterApp(Long along) {
        if (!along.equals(showTime)) {
            splashTime.setText(getString(R.string.splash_time, showTime - along));
        } else {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}