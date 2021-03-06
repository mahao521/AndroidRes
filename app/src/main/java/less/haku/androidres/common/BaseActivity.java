package less.haku.androidres.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import less.haku.androidres.application.HApplication;
import less.haku.androidres.request.base.BaseRequest;
import less.haku.androidres.request.base.HOkHttpClient;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by HaKu on 15/11/6.
 * 当前应用中所有的Activity都应该继承此类或此类的子类，
 * 在创建或销毁时，会通知Application，方便app生命周期管理
 */
public class BaseActivity extends AppCompatActivity {

    protected CompositeSubscription compositeSubscription;

    protected boolean isDestoryed;      //标识此Activity是否已销毁
    //持有当前json请求，activity销毁时，遍历并cancel掉所有请求
    protected ArrayList<WeakReference<BaseRequest>> listJsonRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Activity : ", "Start Activity: " + this.getClass().getName());
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.unsubscribe();

        isDestoryed = true;
        Log.v("Activity : ", this.getClass().getName());
        if (listJsonRequest != null) { //遍历取消所有请求
            for (WeakReference<BaseRequest> ref : listJsonRequest) {
                BaseRequest req = ref.get();
                if (req != null) {
                    req.call.cancel();
                }
            }
        }
        super.onDestroy();
    }

    /**
     * 发送请求
     *
     * @param request
     * @param listener
     */
    public void sendJsonRequest(BaseRequest request, HOkHttpClient.IRequestListener listener) {
        hideKeyboard();
        if (listJsonRequest == null) {
            listJsonRequest = new ArrayList<WeakReference<BaseRequest>>();
        }
        WeakReference<BaseRequest> ref = new WeakReference<BaseRequest>(request);
        listJsonRequest.add(ref);
        HOkHttpClient.instance(HApplication.instance()).sendRequest(request, listener);
    }

    /**
     * 隐藏软键盘
     */
    protected void hideKeyboard() {
        View root = ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        if (null == root) {
            return;
        }
        ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                root.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private Toast toast; //创建一个对象，防止多次连续调用

    /**
     * 提供一个方便的弹toast的方法
     */
    protected void showToast(String msg) {

        if (TextUtils.isEmpty(msg)) {
            return;
        }

        showToast(msg, Toast.LENGTH_LONG);
    }

    protected void showToast(String msg, int duration) {

        if (toast == null) {
            toast = Toast.makeText(this, msg, duration);
        } else {
            toast.setText(msg);
            toast.setDuration(duration);
        }

        toast.show();
    }

    protected void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }


    /**
     * ********** dialog *****************
     */
    protected String dlgProgressTitle;
    protected int managedDialogId = 0;
    protected static final int DLG_PROGRESS = 0xFA05;
    protected Dialog managedDialog;

    public void showProgressDialog(String title) {

        if (isDestoryed) {
            return;
        }

        dismissDialog();
        dlgProgressTitle = title;
        ProgressDialog dlg = new ProgressDialog(this);
        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (managedDialogId == DLG_PROGRESS) {
                    managedDialogId = 0;
                }
                dlgProgressTitle = null;
                //onProgressDialogCancel();
            }
        });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        });
        dlg.setMessage(dlgProgressTitle == null ? "载入中..." : dlgProgressTitle);

        managedDialogId = DLG_PROGRESS;
        managedDialog = dlg;
        dlg.show();
    }

    public void dismissDialog() {
        if (isDestoryed) {
            return;
        }
        if (managedDialogId != 0) {
            if ((managedDialog != null) && managedDialog.isShowing()) {
                managedDialog.dismiss();
            }
            dlgProgressTitle = null;
            managedDialogId = 0;
            managedDialog = null;
        }
    }

    /**
     * 显示一个需要二次确认的对话框
     * @param listener
     */
    protected void showEnsureAlert(String title, String message, String btnTitleEnsure, String btnTitleCancel, final EnsureListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }

        builder.setPositiveButton(TextUtils.isEmpty(btnTitleEnsure) ? "确认" : btnTitleEnsure, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.ensure();
                }
            }
        });

        if (!TextUtils.isEmpty(btnTitleCancel)) {
            builder.setNegativeButton(TextUtils.isEmpty(btnTitleCancel) ? "取消" : btnTitleCancel, null);
        }
        AlertDialog dialog = builder.create();
//        dialog.setCancelable(true);
        dialog.show();
    }

    public interface EnsureListener {
        void ensure();
    }
}
