package im.hrgroup.com.hrsmack.util;

import android.support.annotation.Nullable;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Created by 18515 on 2019/3/27.
 */

public class XMPPConnectionTools {

    private String user;
    private String domain;
    private String password;
    private int port = 5222;
    private XMPPTCPConnection connection = null;
    private static volatile XMPPConnectionTools instance = null;

    private ChatManager chatManager;

    private XMPPConnectionTools(String user, String password, String domain) {
        this.user = user;
        this.domain = domain;
        this.password = password;
    }

    public static XMPPConnectionTools getInstance(@Nullable String user, @Nullable String password,@Nullable String domain) {
        if (instance == null) {
            synchronized (XMPPConnectionTools.class){
                if (instance == null){
                    instance = new XMPPConnectionTools(user,password,domain);
                }
            }
        }
        return instance;
    }


    /**
     * 登录
     * @return
     */
    public boolean loginOpenFire(){
        try {
            getConnection().login(user, password);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取系统连接
     * @return
     */
    public XMPPTCPConnection getConnection() {
        if (connection != null) {
            return connection;
        }
        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        try {
            config.setHostAddress(InetAddress.getByName(domain));
            config.setXmppDomain(domain);
            config.setPort(port);
            //禁用SSL连接
            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setCompressionEnabled(false);
            config.setDebuggerEnabled(true);
            //设置离线状态 false会导致IncomeListener无法触发
            config.setSendPresence(true);
            //需要经过同意才可以添加好友
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

            connection = new XMPPTCPConnection(config.build());
            connection.connect();

            connection.setFromMode(XMPPConnection.FromMode.USER);
            connection.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
                    Message headlineMessage = (Message) packet;
                    System.out.println("StanzaListener msg : "+headlineMessage.getBody().toString());
                }
            }, MessageTypeFilter.HEADLINE);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public ChatManager getChatManager(){
        if (chatManager == null) {
            chatManager = ChatManager.getInstanceFor(getConnection());
        }
        return chatManager;
    }

}
