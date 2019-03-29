package im.hrgroup.com.hrsmack.util;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.EntityBareJid;

import im.hrgroup.com.hrsmack.listener.HRRoomMessageListener;

/**
 * Created by 18515 on 2019/3/29.
 */

public class HRChatRoom {

    private MultiUserChat multiUserChat;

    private HRRoomMessageListener listener;

    public HRChatRoom(MultiUserChat multiUserChat, HRRoomMessageListener listener) {
        this.multiUserChat = multiUserChat;
        this.listener = listener;
        this.multiUserChat.addMessageListener(listener);
    }

    /**
     * 添加监听
     * @param listener
     */
    public void addListener(MessageListener listener) {
        if (this.listener != null) {
            removeListener();
        }
        this.multiUserChat.addMessageListener(listener);
    }

    /**
     * 取消监听，防止内存泄漏
     */
    public void removeListener() {
        this.multiUserChat.removeMessageListener(listener);
    }

    /**
     * 返回信息
     * @return
     */
    public String showMessage(){
        return listener.showMessage();
    }

    public EntityBareJid getRoomJid() {
        return this.multiUserChat.getRoom();
    }

    public MultiUserChat getMultiUserChat() {
        return multiUserChat;
    }

    public HRRoomMessageListener getListener() {
        return listener;
    }

    public void sendMsg(XMPPConnectionTools tools, String msg) {
        try {
            EntityBareJid roomJid = this.multiUserChat.getRoom();
            tools.sendChatGroupMessage(roomJid,msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
