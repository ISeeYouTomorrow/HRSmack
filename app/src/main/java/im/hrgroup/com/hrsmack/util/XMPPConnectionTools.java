package im.hrgroup.com.hrsmack.util;

import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String CONFERENCE = "@conference.";

    private ChatManager chatManager;
    private Roster friendRoster = null;//初始化花名册
    private MultiUserChatManager multiUserChatManager;


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
            if (!getConnection().isConnected()) {
                getConnection().connect();
                getConnection().login(user, password);
//                addPacketListener();
            }
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
//            connection.connect();
            connection.setFromMode(XMPPConnection.FromMode.USER);

            //除chat类型的消息通过此处处理
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
        }
        return connection;
    }

    /**
     * 获取历史聊天室
     */
    public void addPacketListener() {
        getConnection().addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza paramPacket) throws SmackException.NotConnectedException, InterruptedException {
                if (paramPacket == null )
                    return;
                System.out.println("========="+ paramPacket.toXML().toString());
                if(paramPacket.getFrom().equals(getConnection().getUser())){
                    String xml = (String) paramPacket.toXML();
                    String from[];

                    System.out.println(xml);
                    from = paramPacket.getFrom().toString().split("/");
                    Pattern pattern = Pattern.compile("<item jid=\"(.*?)/>");
                    Matcher matcher = pattern.matcher(xml);
                    String parts[];

                    while (matcher.find()) {
                        parts=matcher.group(1).split("@");
                        Log.d("part -------- ", parts[0]);
                    }
                    return;
            }
        }},IQTypeFilter.RESULT);
    }

    /**
     * 获取聊天管理
     * @return
     */
    public synchronized ChatManager getChatManager(){
        if (chatManager == null) {
            chatManager = ChatManager.getInstanceFor(getConnection());
        }
        return chatManager;
    }

    /**
     * 发送消息
     * @param receive
     * @param body
     * @throws Exception
     */
    public void sendChatMessage(String receive, String body) throws Exception {
        //接收人jid
        EntityBareJid jid = JidCreate.entityBareFrom(receive);
        //获取聊天chat
        Chat chat = getChatManager().chatWith(jid);

        //创建消息对象，消息类型是Message.Type.chat
        Message message = new Message(jid, Message.Type.chat);
        message.setBody(body);
        //发送消息
        chat.send(message);
    }

    /**
     * 先获取离线记录，然后设置登录状态为在线。
     * @return
     */
    public List<Message> getOfflineMessage() {
        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(getConnection());
        try {
            //获取离线信息
            List<Message> messages = offlineMessageManager.getMessages();

            //获取离线消息的数量并删除
            int count = offlineMessageManager.getMessageCount();
            offlineMessageManager.deleteMessages();

            Presence presence = new Presence(Presence.Type.available);
            connection.sendStanza(presence);
            return messages;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 初始化好友记录
     * @return
     */
    public synchronized Roster getFriendRoster() {
        if (friendRoster == null) {
            friendRoster = Roster.getInstanceFor(getConnection());
        }
        return friendRoster;
    }

    /**
     * 获取好友实体
     * @return
     */
    public Set<RosterEntry> getFriends() {
        Set<RosterEntry> friends = getFriendRoster().getEntries();
        return friends;
    }

    /**
     * 退出连接
     */
    public void clear() {
        if (connection.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connection.disconnect();
                }
            }).start();
        }
        connection = null;
    }




    /**
     * 创建群群jid的格式就是  群名称@conference.openfire服务器名称
     * @param groupName 房间名称
     * @param users 用户
     * @param nickName 群昵称
     * @return
     */
    public MultiUserChat createChatRoom(String groupName, List<String> users, String nickName) {
        String jid = fromJID(groupName);
        try {
            EntityBareJid groupJid = JidCreate.entityBareFrom(jid);
            MultiUserChat muc = getMultiUserChatManager().getMultiUserChat(groupJid);
            muc.create(Resourcepart.from(nickName));

            Form form = muc.getConfigurationForm();//聊天室的配置单
            Form submitForm = form.createAnswerForm();//创建一个可提交的新表单

            List<String> owners = new ArrayList<>();
            owners.add(user+ "@" + getConnection().getServiceName());
            //用户的jid或者名称
            if(users != null && !users.isEmpty()) {
                for (int i = 0; i < users.size(); i++){  //添加群成员,用户jid格式和之前一样 用户名@openfire服务器名称
                    owners.add(users.get(i));
                }
            }

            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            //设置为公共房间
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 房间仅对成员开放
            submitForm.setAnswer("muc#roomconfig_membersonly", false);
            // 允许占有者邀请其他人
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            //进入不需要密码
            submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",  false);

            // 能够发现占有者真实 JID 的角色
            // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 仅允许注册的昵称登录
            submitForm.setAnswer("x-muc#roomconfig_reservednick", false);
            // 允许使用者修改昵称
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);
            // 允许用户注册房间
            submitForm.setAnswer("x-muc#roomconfig_registration", false);
            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            muc.sendConfigurationForm(submitForm);
//            添加群消息监听
//            muc.addMessageListener(callBack);
            return muc;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.MucAlreadyJoinedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.MissingMucCreationAcknowledgeException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 发送群聊普通消息
     * @param groupName group1@conference.192.168.0.236
     * @param body
     */
    public void sendChatGroupMessage(String groupName, String body) throws Exception {
        //拼凑jid
        String jid = fromJID(groupName);
        //创建jid实体
        EntityBareJid groupJid = JidCreate.entityBareFrom(jid);
        //群管理对象
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(groupJid);
        //发送信息
        multiUserChat.sendMessage(body);
    }

    private String fromJID(String groupName) {
        return groupName + CONFERENCE + connection.getServiceName();
    }


    /**
     * 加入群聊会议室
     * @param groupName
     * @param nickName
     * @return
     * @throws Exception
     */
    public MultiUserChat joinMultiUserChat(String groupName, String nickName) {
        //群jid
        String jid = fromJID(groupName);
        //jid实体创建
        EntityBareJid groupJid = null;
        MultiUserChat multiUserChat = null;
        try {
            groupJid = JidCreate.entityBareFrom(jid);
            //获取群管理对象
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            //通过群管理对象获取该群房间对象
            multiUserChat = multiUserChatManager.getMultiUserChat(groupJid);

            MucEnterConfiguration.Builder builder = multiUserChat.getEnterConfigurationBuilder(Resourcepart.from(nickName));
            //只获取最后99条历史记录
            builder.requestMaxCharsHistory(99);
            MucEnterConfiguration mucEnterConfiguration = builder.build();
            //加入群
            multiUserChat.join(mucEnterConfiguration);

            Log.d("joinRoomListener",""+"加入聊天室"+multiUserChat.getRoom().toString());

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return multiUserChat;
    }

    /**
     * 退出群聊
     * 1：权限、权限、权限！因为如果你进群后，默认角色是普通成员，普通成员也就只有发送消息、接收消息的权限，
     * 如果在操作其他功能，如获取用户列表、踢人这些功能是会报没有权限的错误，所以这个需要给用户赋予管理员或者以上级别。
     * 在进入群合退出群的时候，记得要注册或注销监听器，以免造成内存泄漏
     * @param groupName
     * @throws XmppStringprepException
     */
    public void quitRoom(String groupName) throws Exception {
        String jid = fromJID(groupName);
        EntityBareJid groupJid = JidCreate.entityBareFrom(jid);

        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(groupJid);
        //退出群
        multiUserChat.leave();
    }

    public List<EntityBareJid> getAllRooms() {
        List<EntityBareJid> rooms = new ArrayList<>();
        try {
            List<DomainBareJid> domainBareJids = getMultiUserChatManager().getXMPPServiceDomains();
            for (DomainBareJid domainBareJid : domainBareJids) {
                List<HostedRoom> list = getMultiUserChatManager().getHostedRooms(domainBareJid);
                for (HostedRoom hostedRoom : list) {
                    rooms.add(hostedRoom.getJid());
                }
            }

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    /**
     * 返回历史会议室列表
     * @return
     */
    public List<EntityBareJid> getHistoryRooms() {
        try {
            if(getMultiUserChatManager().isServiceEnabled(getConnection().getUser())){
                EntityBareJid jid = JidCreate.entityBareFrom(getConnection().getUser());
                Log.d("getHistoryRooms", ""+jid);

                Set<EntityBareJid> ss = getMultiUserChatManager().getJoinedRooms();
                for (EntityBareJid s : ss) {
                    Log.d("getJoinedRooms", ""+s);
                }

                return getMultiUserChatManager().getJoinedRooms(jid);
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取会议室管理
     * @return
     */
    public synchronized MultiUserChatManager getMultiUserChatManager() {
        if(multiUserChatManager == null){
            multiUserChatManager = MultiUserChatManager.getInstanceFor(getConnection());
        }
        return multiUserChatManager;
    }
}
