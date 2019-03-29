package im.hrgroup.com.hrsmack.listener;

import android.util.Log;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Calendar;

import im.hrgroup.com.hrsmack.util.HRCallBack;

/**
 * Created by 18515 on 2019/3/28.
 */

public class HRRoomMessageListener implements MessageListener {
    private StringBuilder sb = new StringBuilder() ;
    private HRCallBack callBack;
    public HRRoomMessageListener(HRCallBack callBack){
        this.callBack = callBack;
    }
    @Override
    public void processMessage(Message message) {
        Log.d("income from: ", ""+message.getFrom().toString());
        //消息内容
        Log.d("income receive: ", ""+message.getBody());

        sb.append(Calendar.getInstance().getTime().toString()).append(" ").append(message.getFrom().toString())
                .append("\r").append(message.getBody());
        sb.append("\r\n");
        Log.d("HRMessageListener: ", ""+sb.toString());
        if (callBack != null) {
            callBack.doCallBack(sb.toString());
        }
    }

    public String showMessage() {
        return sb.toString();
    }
}
