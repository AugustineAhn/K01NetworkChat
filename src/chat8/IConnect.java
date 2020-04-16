package chat8;

public interface IConnect {
	
	
	/*
	멤버상수
		: interface에 선언된 변수는 무조건 public static final
		이 붙어 정적상수로 선언된다.
	 */
	String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	String ORACLE_URL = "jdbc:oracle:thin://@localhost:1521:orcl";
	String ID = "KOSMO";
	String PASS = "1234";
	
	
	//멤버추상메소드	: public sbstract 가 붙어 추상메소드로 선언된다.
	void connect();//DB 연결
	void close();// 자원반납
	
}
