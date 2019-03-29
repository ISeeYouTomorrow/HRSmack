package im.hrgroup.com.hrsmack;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import im.hrgroup.com.hrsmack.listener.HRChatMessageListener;
import im.hrgroup.com.hrsmack.listener.HRMessageListener;
import im.hrgroup.com.hrsmack.util.HRCallBack;
import im.hrgroup.com.hrsmack.util.XMPPConnectionTools;

public class ChatActivity extends Activity implements View.OnClickListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Button friendBtn, roomBtn, msgBtn;

    private static TextView msgView, roomMsgView;
    private StringBuilder sb = new StringBuilder();

    private static StringBuilder roomSb = new StringBuilder();

    private static XMPPConnectionTools tools ;
    private ChatManager chatManager;

    private HRCallBack showChatMsg = new HRCallBack() {
        @Override
        public void doCallBack(final String... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    msgView.setText(args[0]);
                }
            });
        }
    };

    private HRChatMessageListener hrChatMessageListener = new HRChatMessageListener(sb, showChatMsg);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        tools = ((HRApplication)getApplication()).getXmpp();
        chatManager = tools.getChatManager();
        chatManager.addIncomingListener(hrChatMessageListener);
        chatManager.addOutgoingListener(hrChatMessageListener);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        friendBtn = findViewById(R.id.friendBtn);
        roomBtn = findViewById(R.id.roomBtn);
        msgBtn = findViewById(R.id.msgBtn);

        friendBtn.setOnClickListener(this);
        roomBtn.setOnClickListener(this);
        msgBtn.setOnClickListener(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.friendBtn:
                mViewPager.setCurrentItem(0, true);
                break;
            case R.id.roomBtn:
                mViewPager.setCurrentItem(1, true);
                break;
            case R.id.msgBtn:
                mViewPager.setCurrentItem(2, true);
                break;
            default:
                break;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private EditText sendToUser;
        private EditText sendContent;
        private Button sendBtn;


        private ListView historyRooms;
        private List<String> historyRoomData;
        private ArrayAdapter roomAdaper;
        private EditText roomName,roomUser,sendRoomMsg;
        private Button createRoomBtn,sendRoomMsgBtn;
        private View roomChatShow;
        private MultiUserChat currentMultiUserChat;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private HRCallBack showRoomChatMsg = new HRCallBack() {
            @Override
            public void doCallBack(final String... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        roomMsgView.setText("" + args[0]);
                    }
                });
            }
        };
        private HRMessageListener messageListener = new HRMessageListener(roomSb, showRoomChatMsg);

        View.OnClickListener sendMsgListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String to = sendToUser.getText().toString();
                String content = sendContent.getText().toString();
                try {
                    tools.sendChatMessage(to, content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        /**
         * 创建聊天室
         */
        View.OnClickListener createRoomListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rName = roomName.getText().toString();
                String rUser = roomUser.getText().toString();
                List<String> users = new ArrayList<>();
                String[] rus = rUser.split(";");
                if (rus != null && rus.length > 0) {
                    for (String s : rus) {
                        users.add(s);
                    }
                }
                MultiUserChat room = tools.createChatRoom(rName, users, rName);
                Log.d("createRoomListener", "创建聊天室成功:" + room);
                room.addMessageListener(messageListener);
                String currentRoomName = room.getRoom().toString();
                currentMultiUserChat = room;
                Log.d("currentRoomName", "当前聊天室的名称:" + currentRoomName);
                roomChatShow.setVisibility(View.VISIBLE);
                historyRoomData.add(currentRoomName);
                roomAdaper.notifyDataSetChanged();
            }
        };

        /**
         * 发送聊天室消息
         */
        private View.OnClickListener sendRoomMsgListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = roomName.getText().toString();
                String msg = sendRoomMsg.getText().toString();
                try {
                    tools.sendChatGroupMessage(groupName, msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        private AdapterView.OnItemLongClickListener joinRoomListener = new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String groupName = historyRoomData.get(position).split("@")[0];
                MultiUserChat multiUserChat = tools.joinMultiUserChat(groupName,"小猪快跑");
                multiUserChat.addMessageListener(messageListener);
                currentMultiUserChat = multiUserChat;
                return false;
            }
        };


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            switch (sectionNumber) {
                case 1:
                    rootView = inflater.inflate(R.layout.friend_layout, container, false);
                    sendToUser = rootView.findViewById(R.id.sendToUser);
                    sendContent = rootView.findViewById(R.id.sendContent);
                    sendBtn = rootView.findViewById(R.id.sendBtn);
                    msgView = rootView.findViewById(R.id.receiveMsg);
                    sendBtn.setOnClickListener(sendMsgListener);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.room_layout, container, false);
                    historyRooms = rootView.findViewById(R.id.historyRooms);
                    roomUser = rootView.findViewById(R.id.roomUser);
                    roomName = rootView.findViewById(R.id.roomName);
                    createRoomBtn = rootView.findViewById(R.id.createRoomBtn);
                    createRoomBtn.setOnClickListener(createRoomListener);
                    roomChatShow = rootView.findViewById(R.id.roomChatShow);
                    sendRoomMsgBtn = rootView.findViewById(R.id.sendRoomMsgBtn);
                    sendRoomMsgBtn.setOnClickListener(sendRoomMsgListener);
                    sendRoomMsg = rootView.findViewById(R.id.room_send_msg);
                    roomMsgView = rootView.findViewById(R.id.roomMsg);

                    initRoomView(container.getContext());
                    break;
            }
            return rootView;
        }

        private AsyncTask roomTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                List<EntityBareJid> rooms = tools.getHistoryRooms();
                if (rooms == null || rooms.size() == 0) {
                } else {
                    for (EntityBareJid room : rooms) {
                        historyRoomData.add("" + room.toString());
                    }
                }
                roomAdaper.notifyDataSetChanged();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                roomMsgView.setText("群消息显示在这里...");
            }
        };

        /**
         * 会议室页面
         *
         * @param context
         */
        private void initRoomView(Context context) {
//            List<EntityBareJid> rooms = tools.getAllRooms();
            historyRoomData = new ArrayList<>();
            historyRooms.setOnItemLongClickListener(joinRoomListener);
            roomAdaper = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, historyRoomData);
            roomTask.execute();

        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "好友";
                case 1:
                    return "聊天室";
                case 2:
                    return "个人信息";
            }
            return null;
        }
    }
}
