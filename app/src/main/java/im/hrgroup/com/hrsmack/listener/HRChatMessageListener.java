package im.hrgroup.com.hrsmack.listener;

import android.util.Log;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import java.util.Calendar;

import im.hrgroup.com.hrsmack.util.HRCallBack;

/**
 * Created by 18515 on 2019/3/28.
 */

public class HRChatMessageListener implements IncomingChatMessageListener,OutgoingChatMessageListener {

    private StringBuilder sb;
    private HRCallBack callBack;
    public HRChatMessageListener(StringBuilder sb, HRCallBack callBack) {
        this.sb = sb;
        this.callBack = callBack;
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        //from就是消息的发送方的jid，message即发送的消息对象，chat也就是单聊对象
        //获取from发送方的账号，转换成string即可
        Log.d("income from: ", from.toString());
        //消息内容
        Log.d("income receive: ", message.getBody());

        sb.append(Calendar.getInstance().getTime().toString()).append(" ").append(from.toString())
                .append("\r").append(message.getBody());
        sb.append("\r\n");
        if (callBack != null) {
            callBack.doCallBack(sb.toString());
        }
//        放到call里执行
//        context.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                msgView.setText(sb.toString());
//            }
//        });
    }

    @Override
    public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
        //from就是消息的发送方的jid，message即发送的消息对象，chat也就是单聊对象
        //获取from发送方的账号，转换成string即可
        Log.d("outgoing to:", to.toString());
        //消息内容
        Log.d("outgoint send:", message.getBody());

        sb.append(Calendar.getInstance().getTime().toString()).append(" ").append(to.toString())
                .append("\r").append(message.getBody());
        sb.append("\r\n");

        if (callBack != null) {
            callBack.doCallBack(sb.toString());
        }
    }

    public String showMessage() {
        return sb.toString();
    }
}
