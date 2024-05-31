import java.sql.*;
import java.util.ArrayList;
import java.util.List;

interface AuthService{
    void start();
    String getNickByLoginPass(String login, String pass);
    void stop();
    String getNickByLoginPassDB(String login, String pass) throws SQLException, ClassNotFoundException;
    boolean addUser (int id,String login, String pass, String nick) throws SQLException, ClassNotFoundException;
    boolean deleteUser(String nick) throws SQLException, ClassNotFoundException;
    boolean changeName(String newnick, String nick) throws SQLException, ClassNotFoundException;
}

public class BaseAuthService implements AuthService{
    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;
    Connection con;
    PreparedStatement statement;

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }

    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("login1", "pass1", "nick1"));
        entries.add(new Entry("login2", "pass2", "nick2"));
        entries.add(new Entry("login3", "pass3", "nick3"));
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }
    public void connect() throws SQLException, ClassNotFoundException{
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:users.db");

    }

    public String getNickByLoginPassDB(String login, String pass) throws SQLException, ClassNotFoundException{
        connect();
        String returnNick = null;
        statement = con.prepareStatement("Select nick From users WHERE login = ? AND password = ?");
        statement.setString(1, login);
        statement.setString(2, pass);
        ResultSet rs = statement.executeQuery();
        while(rs.next()){
            returnNick = rs.getString("nick");
        }
        con.close();
        return returnNick;
    }
    public synchronized boolean addUser (int id,String login, String pass, String nick) throws SQLException, ClassNotFoundException{
        connect();
        try {
            String query = "INSERT into Users (id ,login, password, nick) VALUES (?,?,?,?)";
            PreparedStatement pr = con.prepareStatement(query);
            pr.setInt(1, id);
            pr.setString(2, login);
            pr.setString(3, pass);
            pr.setString(4, nick);
            pr.executeUpdate();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public synchronized boolean deleteUser(String nick) throws SQLException, ClassNotFoundException {
        connect();
        try {
            String query = "DELETE from Users where nick = ?";
            PreparedStatement pr = con.prepareStatement(query);
            pr.setString(1, nick);
            pr.executeUpdate();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public synchronized boolean changeName(String newnick, String nick) throws SQLException, ClassNotFoundException {
        connect();
        try {
            String query = "UPDATE Users SET nick = ? WHERE nick = ?";
            PreparedStatement pr = con.prepareStatement(query);
            pr.setString(1, newnick);
            pr.setString(2, nick);
            nick = newnick;
            pr.executeUpdate();
            con.close();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
