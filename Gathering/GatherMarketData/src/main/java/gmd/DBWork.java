package gmd;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
//import java.sql.Date;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Date;

public class DBWork {
	
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	   static final String DATABASE_URL = "jdbc:mysql://localhost/torodoro?characterEncoding=utf8";
	    
	   static final String USER = "torodioro";
	   
	   
	public static List<String> getClients(String botName) { // Selects all customers for given botName 
		Stream<Character> pwd = Stream.of('W','R','7','8','d','+','+','X','!','!');
		List<String> result = new ArrayList<>();
		String password = pwd.collect(StringBuilder::new, (sb, ch) -> sb.append(ch), (sb, ch) -> sb.append(ch)).toString();  
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			String sql = "SELECT CHANEL_NAME, TILL FROM periods WHERE BOT_NAME=N'" + botName + "'";
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				if(resultSet.getDate("TILL").after(new Date()))
					result.add(resultSet.getString("CHANEL_NAME"));
			}
	        resultSet.close();
	        statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
