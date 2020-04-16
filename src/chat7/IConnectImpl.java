package chat7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class IConnectImpl implements IConnect{

	
	//동적쿼리를 위한 객체
		public Connection con;
		public PreparedStatement psmt;//(dataInput() 이용
		public ResultSet rs;
		public Statement stmt;
		
		
		public IConnectImpl() {
			
			try {
				//드라이버 로드
				Class.forName(ORACLE_DRIVER);
				//DB연결
				connect();
			} catch (ClassNotFoundException e) {
				System.out.println("드라이버 로딩 실패");
				e.printStackTrace();
			}
		}
		
		@Override
		public void connect() {

			try {
				con = DriverManager.getConnection(ORACLE_URL, ID, PASS);
				if(con!=null)	System.out.println("DB연결됨");
			} catch (SQLException e) {
				System.out.println("데이터페이스 연결 오류");
				e.printStackTrace();
			}		
		}

		@Override
		public void close() {
			try {
				if(con!=null)	con.close();
				if(psmt!=null)	psmt.close();
				if(rs!=null)	rs.close();
				if(stmt!=null)	stmt.close();
				System.out.println("자원반납완료");
			} catch (Exception e) {
				System.out.println("자원반납시에러");
				e.printStackTrace();
			}
			
		}

	
	
	
	
}
