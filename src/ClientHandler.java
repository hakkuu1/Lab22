import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                try {
                    String[] parts = str.split("\\s");
                    //auth login pass
                    String nick = myServer.getAuthService().getNickByLoginPassDB(parts[1], parts[2]);
                    if (nick != null) {
                        if (!myServer.isNickBusy(nick)) {
                            sendMsg("Hello " + nick);
                            name = nick;
                            myServer.broadcastMsg(name + " зашел в чат");
                            myServer.subscribe(this);
                            return;
                        } else {
                            sendMsg("Учетная запись уже используется");
                        }
                    } else {
                        sendMsg("Неверные логин/пароль");
                    }
                } catch (ClassNotFoundException | SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void readMessages() throws IOException, SQLException, ClassNotFoundException {
        while (true) {
            String strFromClient = in.readUTF();
            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals("/end")) {
                return;
            }
            if (strFromClient.startsWith("/w")) {
                String[] parts = strFromClient.split("\\s");

                if (myServer.isNickBusy(parts[1])) {
                    myServer.broadcastMsgToNick(name, parts[1], parts[2]);

                } else {
                    myServer.broadcastMsg(name + ": " + strFromClient);
                }
            } else {
                myServer.broadcastMsg(name + ": " + strFromClient);
            }
            if (strFromClient.startsWith("/changenick")) {
                String[] parts = strFromClient.split("\\s");
                String newnick = parts[1];
                String nick = parts[2];
                myServer.getAuthService().changeName(newnick, nick);
                myServer.broadcastMsg(name + " сменил ник на " + newnick);

            }
            if (this.name.equals("admin")){
                if (strFromClient.startsWith("/adduser")) {
                    String[] parts = strFromClient.split("\\s");
                    String  id = parts[1];
                    String login = parts[2];
                    String pass = parts[3];
                    String nick = parts[4];
                    myServer.getAuthService().addUser(Integer.parseInt(id), login, pass, nick);
                    myServer.broadcastMsg(name + "user " + login + " added");

                }
                if (strFromClient.startsWith("/deluser")) {
                    String[] parts = strFromClient.split("\\s");
                    String nick = parts[1];
                    myServer.getAuthService().deleteUser(nick);
                    myServer.broadcastMsg(name + "user " + nick + " deleted");

                }
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
