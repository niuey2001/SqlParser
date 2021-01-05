import java.io.IOException;
import java.sql.SQLException;

public class SqlParserRunable {
	public static void main(String args[]) throws IOException,
			ClassNotFoundException, SQLException {
		System.out
				.println("...................................................................................................... \n\n"
						+ "SqlParser\n"
						+ "author : Niu && Zhu\n"
						+ "args[]:\n"
						+ "1、解析dds文件路径\n"
						+ "2、数据库IP\n"
						+ "3、数据库端口\n"
						+ "4、数据库实例\n"
						+ "5、数据库用户\n"
						+ "6、数据库密码\n"
						+ "7、数据库类型: oracle 、mysql\n"
						
					 
						+"example :  java -jar SqlParser.jar c:/test 192.168.1.1 1521 jcpt1 xxx xxx oracle \n\n"
						+ ".....................................................................................................\n\n");
		if (args.length == 7) {
 			SqlParser.init(args[0], args[1], args[2], args[3],args[4],args[5],args[6]);

 		}
		else
		{
			System.out.println("请按要求输入参数\n");

		}

	}

}
