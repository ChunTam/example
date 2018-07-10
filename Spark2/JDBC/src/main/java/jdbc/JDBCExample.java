package jdbc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

public class JDBCExample {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Making connection");
        Connection con = DriverManager.getConnection(
                "jdbc:hive2://sparkd4.sec.support.com:10016/default", "hive", "");
        Statement stmt = con.createStatement();
        System.out.println("Done connecting");


        DatabaseMetaData md = con.getMetaData();
        //ResultSet rs = md.getSchemas();
        ResultSet rs = md.getTables(null,null,null,null);
        while (rs.next()) {
            System.out.println(rs.getString(3));
        }
        rs.close();
        stmt.close();
        con.close();
    }
}
