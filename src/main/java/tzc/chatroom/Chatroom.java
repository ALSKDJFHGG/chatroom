package tzc.chatroom;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Chatroom {
    private JPanel panel1;
    private JTextField messageField;
    private JButton sendButton;
    private JList messageList;

    private DefaultListModel messageListModel;
    private String lastestMessageTime="";

    private boolean stopflag=false;

    private String ROOM_URL="http://chatroom.codingpython.cn/chatroom/messages?token={0}&room={1}";
    private static final String SEND_URL="http://chatroom.codingpython.cn/chatroom/chat?token={0}&room={1}&message={2}";
    private String room="";
    private String token="";

    public Chatroom() {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(MessageFormat.format(SEND_URL, token, room, message)).build();
                try(Response response=okHttpClient.newCall(request).execute()) {
                    List<Map> latest20Message = getLatest20Message();
                    String tempTime=null;
                    for (Map item:latest20Message){
                        if (((String)item.get("created_at")).compareTo(lastestMessageTime)>0){
                            messageListModel.addElement(MessageFormat.format("{0}({1}): {2}",item.get("name"),item.get("created_at"),item.get("message")));
                            tempTime=((String)item.get("created_at"));
                        }
                    }
                    if (tempTime!=null){
                        lastestMessageTime=tempTime;
                    }
                    messageList.ensureIndexIsVisible(messageListModel.size()-1);
                    messageField.setText("");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private List<Map> getLatest20Message(){
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MessageFormat.format(ROOM_URL, token,room)).build();
        List<Map> messages=null;
        try (Response response = httpClient.newCall(request).execute()) {
            String content=response.body().string();
            Gson gson = new Gson();
            Map responseJson = gson.fromJson(content, Map.class);
            messages = (List<Map>)responseJson.get("messages");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }

    public  void run(String room,String token) {
        this.room=room;
        this.token=token;
        JFrame frame = new JFrame("Chatroom");
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        messageListModel=new DefaultListModel();
        messageList.setModel(messageListModel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                stopflag=true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopflag) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println("going to run!!!"+room);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    List<Map> latest20Message = getLatest20Message();
                    String tempTime=null;
                    for (Map item:latest20Message){
                        if (((String)item.get("created_at")).compareTo(lastestMessageTime)>0){
                            messageListModel.addElement(MessageFormat.format("{0}({1}): {2}",item.get("name"),item.get("created_at"),item.get("message")));
                            tempTime=((String)item.get("created_at"));
                        }
                    }
                    if (tempTime!=null){
                        lastestMessageTime=tempTime;
                    }
                    messageList.ensureIndexIsVisible(messageListModel.size()-1);
                }
            }
        }).start();

    }
    /*
    public static void main(String[] args) {
        new Chatroom().run();
    }
     */

}
