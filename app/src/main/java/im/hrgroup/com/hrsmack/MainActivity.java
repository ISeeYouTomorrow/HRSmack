package im.hrgroup.com.hrsmack;

import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.Calendar;

import im.hrgroup.com.hrsmack.util.XMPPConnectionTools;

public class MainActivity extends AppCompatActivity{

    private EditText sendToUser, sendContent;
    private Button sendBtn;
    private TextView msgView;
    private StringBuilder sb = new StringBuilder();
    private XMPPConnectionTools tools ;
    private ChatManager chatManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tools = ((HRApplication)getApplication()).getXmpp();
        chatManager = tools.getChatManager();
        chatManager.addIncomingListener(in);
        chatManager.addOutgoingListener(out);

        sendToUser = findViewById(R.id.sendToUser);
        sendContent = findViewById(R.id.sendContent);

        sendBtn = findViewById(R.id.sendBtn);
        msgView = findViewById(R.id.receiveMsg);

        sendBtn.setOnClickListener(sendMsgListener);
    }

    OutgoingChatMessageListener out = new OutgoingChatMessageListener() {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    msgView.setText(sb.toString());
                }
            });
        }
    };

    View.OnClickListener sendMsgListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String to = sendToUser.getText().toString();
            String content = sendContent.getText().toString();

            try {
                sendChatMessage(to, content);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    IncomingChatMessageListener in = new IncomingChatMessageListener() {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    msgView.setText(sb.toString());
                }
            });
        }
    };

    public void sendChatMessage(String receive, String body) throws Exception {
        //接收人jid
        EntityBareJid jid = JidCreate.entityBareFrom(receive);
        //获取聊天chat
        Chat chat = chatManager.chatWith(jid);

        //创建消息对象，消息类型是Message.Type.chat
        Message message = new Message(jid, Message.Type.chat);
        message.setBody(body);
        //发送消息
        chat.send(message);
    }
}
