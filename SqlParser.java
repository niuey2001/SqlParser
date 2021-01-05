import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.util.JdbcConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SqlParser {

	private static String readFileContent(File file) throws IOException {

		Boolean withbz = false;

		System.out.println("dds文件  : " + file.getName() + " 解析开始");
		InputStreamReader isr =new InputStreamReader(new FileInputStream(file),"UTF-8");
		BufferedReader bf = new BufferedReader(isr);
		String content = "";
		StringBuilder sb = new StringBuilder();
		content = zdy(bf.readLine());
		while (content != null) { 
			if (content.contains("WITH")) {
				withbz = true;
			}

			if (content.contains("INSERT") && withbz) {
				content = " SELECT 1 ;" + content;
			}

			sb.append(content);  
			sb.append("\n");  

			content = zdy(bf.readLine());  
		}

		bf.close();
		return sb.toString();

	}

	private static void parsql(String dds, String sql, String ip, String port,
			String user, String pass, String db, String dbtype)
			throws ClassNotFoundException, SQLException {
 		String dbType = JdbcConstants.HIVE;
		// PGSQLStatementParser pa=new PGSQLStatementParser(sql);
		// 格式化输出
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		// List<SQLStatement> stmtList = pa.parseStatementList();

		// 解析出的独立语句的个数
		System.out.println("解析独立sql个数 : " + stmtList.size() + ".");
		System.out.println("	操作		表 ");
		for (int i = 0; i < stmtList.size(); i++) {

			SQLStatement stmt = stmtList.get(i);
			HiveSchemaStatVisitor visitor = new HiveSchemaStatVisitor();
			stmt.accept(visitor);

			Map map = visitor.getTables();
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();

				System.out.print("	" + map.get(key) + "		" + key + "		\n");

				in_db(dds, key.toString(), map.get(key).toString(), "Y", "",
						ip, port, user, pass, db, dbtype);
			}

		 
		}
		System.out.println("dds文件  : " + dds + " 解析结束\n");

	}

	private static void in_db(String dds_name, String yl_table, String czlx,
			String bz, String err, String ip, String port, String db,
			String user, String pass, String dbtype)
			throws ClassNotFoundException, SQLException {
		Connection con;

		if (dbtype.equals("oracle")) {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
					+ port + ":" + db, user, pass);
		} else {
			Class.forName("com.mysql.jdbc.Driver");

			con = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port
					+ "/" + db + "?characterEncoding=UTF-8", user, pass);

		}

		String sql = "insert into dds_depends (dds_name,yl_tables,czlx,err,bz) values (?,?,?,?,?)";
		PreparedStatement stmt = con.prepareStatement(sql);

		if (bz == "Y") {
			stmt.setString(1, dds_name);
			stmt.setString(2, yl_table);
			stmt.setString(3, czlx);
			stmt.setString(4, "");
			stmt.setString(5, "Y");

		} else {
			stmt.setString(1, dds_name);
			stmt.setString(2, "");
			stmt.setString(3, czlx);
			stmt.setString(4, err);
			stmt.setString(5, "N");

		}

		stmt.executeUpdate();
		con.close();

	}

	private static String zdy(String cs) {
		if (cs != null) {
			cs = cs.toUpperCase();

 
			if (cs.contains("INTO")) {

				String pd = cs.substring(cs.indexOf("into") + 4, cs.length());
				if (!pd.contains("TABLE")) {

					cs = cs.replaceAll("INTO", " INTO TABLE ");
				}
			}
		}
		return cs;

	}

	public static void init(String filename, String ip, String port, String db,
			String user, String pass, String dbtype)
			throws ClassNotFoundException, SQLException, IOException

	{
		int fileNum = 0, folderNum = 0;
		File file = new File(filename);
		if (file.exists() && file.isDirectory()) {
			LinkedList<File> list = new LinkedList<File>();
			File[] files = file.listFiles();
			for (File file2 : files) {

				try {
					if (file2.isDirectory()) {
						list.add(file2);
						folderNum++;
					} else {

						parsql(file2.getName(), readFileContent(file2), ip,
								port, db, user, pass, dbtype);
						fileNum++;

					}

				} catch (Exception e) {
					try {
						in_db(file2.getName(), "", "", "N", e.getMessage(), ip,
								port, db, user, pass, dbtype);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.print(e);
				}
				continue;

			}
			File temp_file;
			while (!list.isEmpty()) {
				temp_file = list.removeFirst();
				files = temp_file.listFiles();
				for (File file2 : files) {
					try {
						if (file2.isDirectory()) {
							list.add(file2);
							folderNum++;
						} else {
							parsql(file2.getName(), readFileContent(file2), ip,
									port, db, user, pass, dbtype);
							fileNum++;
						}

					} catch (Exception e) {
						try {
							in_db(file2.getName(), "", "", "N", e.getMessage(),
									ip, port, db, user, pass, dbtype);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						System.out.print(e);
					}
					continue;

				}
			}
		} else if (file.exists() && file.isFile()) {
			parsql(file.getName(), readFileContent(file), ip, port, db, user,
					pass, dbtype);
			fileNum++;
		} else

		{
			System.out.println("文件不存在!");
		}
		System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);

	}
}
