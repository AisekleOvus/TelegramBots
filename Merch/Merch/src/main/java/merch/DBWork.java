package merch;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Date;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.ArrayList;
//import java.util.Date;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DBWork {
	
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	   static final String DATABASE_URL = "jdbc:mysql://localhost/torodoro?characterEncoding=utf8";
	    
	   static final String USER = "torodioro";

	public static Map<Integer, List<String>> getCusromerSubscriptions(String chatId) {
		Map<Integer, List<String>> result = new HashMap<>();
		String password = getPassword();  
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			String sql = "SELECT p.PRIMARY_KEY, p.CHANEL_NAME, BOT_NAME, SINCE, TILL FROM periods p RIGHT JOIN chanels c ON p.CHANEL_NAME = c.CHANEL_NAME"
					+ " WHERE ID='" + chatId + "' ORDER BY p.CHANEL_NAME ";
			                             
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				LocalDate today = LocalDate.now();
				LocalDate till = resultSet.getDate("TILL").toLocalDate();
				if(till.isAfter(today)) {
					long residue = today.until(till, ChronoUnit.DAYS);
					List<String> row = new ArrayList<>();
					row.add(resultSet.getString("CHANEL_NAME"));
					row.add(resultSet.getString("BOT_NAME"));
					row.add(resultSet.getDate("SINCE").toString());
					row.add(resultSet.getDate("TILL").toString());
					row.add(String.valueOf(residue));
				    result.put(Integer.valueOf(resultSet.getInt("PRIMARY_KEY")), row);
				}	
			}
	        resultSet.close();
	        statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	   
    public static Map<Integer, List<String>> getVisiters(String period) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = "daily".equals(period) ? today : "weekly".equals(period) ? today.minusDays(7L) : null;
    	Map<Integer, List<String>> result = new HashMap<>();
		String password = getPassword();  
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			String sql = "SELECT * FROM visiters WHERE DATE BETWEEN DATE '" + weekAgo + "' AND DATE '" + today + "'";
			                             
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				List<String> row = new ArrayList<>();
				row.add(resultSet.getString("FIRST_NAME"));
				row.add(resultSet.getString("LAST_NAME"));
				row.add("@" + resultSet.getString("USERNAME").toString());
				row.add(resultSet.getDate("DATE").toString());
				row.add(resultSet.getTime("TIME").toString());
			    result.put(Integer.valueOf(resultSet.getRow()), row);	
			}
	        resultSet.close();
	        statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return result;
    }

	public static boolean setVisiters(String chatId, String firstName, String lastName, String username) {
		LocalDate visitDate = LocalDate.now();
		LocalTime visitTime = LocalTime.now(); 
		String password = getPassword(); 
		boolean result = false;
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			
			String sqlGetVisitersId = "SELECT ID FROM visiters WHERE ID='" + chatId + "'";
			String sqlInsertIntoVisiters = "INSERT INTO visiters (ID, FIRST_NAME, LAST_NAME, USERNAME, DATE, TIME) VALUES('" 
					+ chatId + "','" + firstName + "','" + lastName + "','" + username + "', DATE '" + visitDate.toString() + "', TIME '" 
					+ visitTime + "')";
			
			
//			if(!statement.executeQuery(sqlGetVisitersId).next())         // выполнение запроса индентификатора посетителя выдало пустой ответ
				result = statement.execute(sqlInsertIntoVisiters);      // создаем соответствующую запись
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static boolean unsubscribe(String pKey) {
		String password = getPassword(); 
		String yesterDay = LocalDate.now().minusDays(1).toString();
		boolean result = true;
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();	
			String sqlUpdateTillField = "UPDATE periods SET TILL= DATE '" + yesterDay + "' WHERE PRIMARY_KEY=" + pKey + "";
			result = statement.execute(sqlUpdateTillField);
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return result;
	}
	public static boolean setClients(String chatId, String username, String chanelName, String botName, String since, String till) {
		String password = getPassword(); 
		String nowaday = LocalDate.now().toString();
		boolean result = true;
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			
			String sqlGetChatID = "SELECT ID FROM customers WHERE ID='" + chatId + "'";
			String sqlGetChanelName = "SELECT CHANEL_NAME FROM chanels WHERE CHANEL_NAME='" + chanelName + "'";
			
			String sqlInsertIntoCustomers = "INSERT INTO customers (ID, USERNAME, DATE) VALUES('" + chatId + "','" + username + "', DATE '" + nowaday + "')";
			String sqlInsertIntoChanels = "INSERT INTO chanels (CHANEL_NAME, ID) VALUES('" + chanelName + "','" + chatId + "')";
			String sqlInsertIntoPeriods = "INSERT INTO periods (BOT_NAME, CHANEL_NAME, SINCE, TILL) VALUES(N'" + botName + "','" + chanelName + "', DATE '" + since + "', DATE '" + till + "')";
//			System.out.println(sqlInsertIntoPeriods);
			if(!statement.executeQuery(sqlGetChatID).next()) // выполнение запроса индентификатора чата выдало пустой ответ
			 statement.execute(sqlInsertIntoCustomers);      // создаем соответствующую запись
			if(!statement.executeQuery(sqlGetChanelName).next()) // выполнение запроса названия канала чата выдало пустой ответ
			 statement.execute(sqlInsertIntoChanels);            // создаем соответствующую запись
			
			result =  statement.execute(sqlInsertIntoPeriods);

	        statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static String getClientActivityPeriod(String botName, String chanelName) {  
		String result = null;
		String password = getPassword(); 
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			String sqlGetClientActivityPeriod = "SELECT CHANEL_NAME, TILL FROM periods WHERE BOT_NAME='" + botName + "' AND CHANEL_NAME='" + chanelName + "'";
			ResultSet resultSet = statement.executeQuery(sqlGetClientActivityPeriod);
            if(resultSet.next())
                result = resultSet.getDate("TILL").toString();
	        resultSet.close();
	        statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static List<String> getClients() { // Selects all customers for given botName
		return getClients(null);
	}
	public static List<String> getClients(String botName) { // Selects all customers for given botName 
        List<String> result = new ArrayList<>();
		String password = getPassword();  
		try(Connection connection = DriverManager.getConnection(DATABASE_URL, USER, password)) {
			Statement statement = connection.createStatement();
			String sql = botName != null ? "SELECT CHANEL_NAME, TILL FROM periods WHERE BOT_NAME='" + botName + "'"
			                             : "SELECT * FROM periods";
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				if(botName != null) {
				    if(resultSet.getDate("TILL").toLocalDate().isAfter(LocalDate.now()))
					    result.add(resultSet.getString("CHANEL_NAME"));
				} else {
					result.add(resultSet.getString("CHANEL_NAME") + "\n" + resultSet.getString("BOT_NAME") + "\nс "
				    + resultSet.getDate("SINCE").toString().replace("-","\\-") + "\nпо " + resultSet.getDate("TILL").toString().replace("-","\\-"));
				}
			}
	        resultSet.close();
	        statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private static String getPassword() {
		Stream<Character> pwd = Stream.of('W','R','7','8','d','+','+','X','!','!');
		return pwd.collect(StringBuilder::new, (sb, ch) -> sb.append(ch), (sb, ch) -> sb.append(ch)).toString();
	}
}
