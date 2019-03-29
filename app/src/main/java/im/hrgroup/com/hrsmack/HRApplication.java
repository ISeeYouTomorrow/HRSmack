package im.hrgroup.com.hrsmack;

import android.app.Application;
import android.util.Log;

import im.hrgroup.com.hrsmack.util.XMPPConnectionTools;

/**
 * Created by 18515 on 2019/3/27.
 */

public class HRApplication extends Application {

    private XMPPConnectionTools xmpp;

    public XMPPConnectionTools getXmpp() {
        return xmpp;
    }

    public void setXmpp(XMPPConnectionTools xmpp) {
        this.xmpp = xmpp;
    }

    @Override
    public void onTerminate() {
        xmpp.clear();

        Log.d("HRApplication", "terminate need clear");
        super.onTerminate();
    }
}
