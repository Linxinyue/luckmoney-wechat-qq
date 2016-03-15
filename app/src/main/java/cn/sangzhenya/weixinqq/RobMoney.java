package cn.sangzhenya.weixinqq;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;


public class RobMoney extends AccessibilityService {
    private ArrayList havaGot=new ArrayList();
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (content.contains("红包")) {
                            KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                            boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();

                            if (flag) {
                                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
                                wl.acquire();
                                wl.release();

                                KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                                KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
                                kl.disableKeyguard();
                            }
                            if (event.getParcelableData() != null
                                    &&
                                    event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                Log.e("service",event.getClassName().toString());
                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    getPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    openPacket();
                } else if (className.equals("com.tencent.mobileqq.activity.SplashActivity")) {
                    getQQ();
                }
                break;
        }
    }

    private void getQQ() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        recycleQQ(rootNode);
    }
    private void recycleQQ(AccessibilityNodeInfo info){
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                if ("点击拆开".equals(info.getText().toString())) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }

                }
            }

        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycleQQ(info.getChild(i));
                }
            }
        }
    }


    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        recycle(rootNode);
    }

    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                if ("领取红包".equals(info.getText().toString())) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            if(!havaGot.contains(info.toString().substring(48,57))){
                                havaGot.add(info.toString().substring(48,57));
                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }else {
                                Log.i("services","领过了！");
                            }
                            break;
                        }
                        parent = parent.getParent();
                    }

                }
            }

        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    /**
     * 查找到
     */
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");
            for (AccessibilityNodeInfo n : list) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }


}
