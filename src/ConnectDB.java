import java.sql.*;

public class ConnectDB {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:users.db");
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM Users");
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                System.out.println(rs.getInt("id") + " " + rs.getString("login") + " " +
                        rs.getString("password") + " " + rs.getString("nick"));
            }
            System.out.println("Connected");
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}