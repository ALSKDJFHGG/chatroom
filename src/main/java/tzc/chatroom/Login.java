package tzc.chatroom;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Login {
    private JPanel panel1;
    private JTextField mobileField;
    private JTextField nameField;
    private JButton loginButton;

    private JFrame frame;

    private static final String LOGIN_URL="http://chatroom.codingpython.cn/login?mobile={0}&name={1}";

    public Login() {

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mobile= mobileField.getText();
                String name= nameField.getText();
                if (mobile==null || mobile.trim().equals("")) {
                    JOptionPane.showMessageDialog(null,"手机号不能为空");
                    return;
                }
                if (name==null || name.trim().equals("")) {
                    JOptionPane.showMessageDialog(null,"姓名不能为空");
                    return;
                }

                OkHttpClient httpClient = new OkHttpClient();
                Request request = new Request.Builder().url(MessageFormat.format(LOGIN_URL, mobile, name)).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    String content = response.body().string();
                    System.out.println(content);

                    Gson gson=new Gson();
                    Map<String, Object> responseJson = gson.fromJson(content,Map.class);
                    Object detail = responseJson.get("detail");

                    if (detail !=null) {
                        JOptionPane.showMessageDialog(null,detail);
                        return;
                    }
                    Object token = responseJson.get("token");
                    if (token !=null) {

                        frame.dispose();

                        String tokenString = token.toString();
                        Lobby lobby = new Lobby();
                        lobby.run(tokenString);

                    }

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void run() {
        frame = new JFrame("Login");
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        new Login().run();
    }
}
