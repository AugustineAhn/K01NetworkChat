package chat8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;



public class MultiServer extends IConnectImpl{

	static ServerSocket serverSocket = null;
	static Socket socket = null;

	//클라이언트 정보 저장을 위한 Map컬렉션 정의
	Map<String, PrintWriter> clientMap;

	//클라이언트의 IP주소를 저장할 set컬렉션
	HashSet<InetAddress> blacklist;
	InetAddress address;

	HashSet<String> banWords=new HashSet<String>();

	//생성자
	public MultiServer() {
		super();

		//클라이언트의 이름과 출력스트림을 저장할 HashMap생성
		clientMap = new HashMap<String, PrintWriter>();
		//HashMap동기화 설정. 쓰레드가 사용자정보에 동시에 접근하는것을 차단한다.
		Collections.synchronizedMap(clientMap);

		blacklist=new HashSet<InetAddress>();
	}


	public void init() {

		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			InetAddress address;
			Scanner scan = new Scanner(System.in);

			while(true) {
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress()+":"
						+socket.getPort());

				address=socket.getInetAddress();

				System.out.println("블랙리스트 추가: 1");
				System.out.println("블랙리스트 추가: 2");
				System.out.println("접속허용은 아무키나 누르시오");

				String input=scan.nextLine();

				if (input.equals("1")) {
					System.out.println("블랙리스트 추가" + blacklist.add(address));
				}
				if (input.equals("2")) {
					System.out.println("블랙리스트 명단");
					System.out.println(blacklist);
				}
				if (blacklist.contains(address)) {
					System.out.println("2");
					socket=null;
					System.out.println(address+" 는 차단되었습니다. ");
					continue;
				}
				/*
				클라이언트의 메세지를 모든 클라이언트에게 전달하기 위한
				쓰레드 생성 및 start.
				 */
				Thread mst = new MultiServerT(socket);
				mst.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//메인메소드 : Server객체를 생성한후 초기화한다.
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}


	//접속된 모든 클라이언트에게 메세지를 전달하는 역할의 메소드
	public void sendAllMsg(String name, String msg) {
		//Map에 저장된 객체의 키값(이름)을 먼저 얻어온다.
		Iterator<String> it = clientMap.keySet().iterator();

		//저장된 객체(클라이언트)의 갯수만큼 반복한다.
		while(it.hasNext()) {
			try {
				//각 클라이언트의 PrintWriter객체를 얻어온다.
				PrintWriter it_out = (PrintWriter)clientMap.get
						(it.next());

				//클라이언트에게 메세지를 전달한다.
				/*
				매개변수 name이 있는 경우에는 이름+메세지
				없는경우에는 메세지만 클라이언트로 전달한다.
				 */
				if(name.equals("")) {
					//해쉬맵에 저장되어있는 클라이언트들에게 메세지를 전달한다.
					//따라서 접속자를 제외한 나머지 클라이언트만 입장메세지를 받는다.
					it_out.println(URLEncoder.encode(msg,"UTF-8"));
				}
				else {
					it_out.println("["+name+"]:"+msg);
				}


			}
			catch (Exception e) {
				System.out.println("예외:"+e);

			}
		}
	}

	//중복확인
	public String checkDuplicate(String name) {
		int addName=1;
		String copy=name;
		while (clientMap.containsKey(copy)) {
			System.out.println("중복발견");
			copy+= addName++;
		}
		System.out.println("중복체크 최종결과 "+copy);
		return copy;
	}


	//금칙어 사용금지
	public String banWords(String str) {

		Iterator<String> it=banWords.iterator();
		String text=str;
		while (it.hasNext()) {
			String word=it.next();
			if (str.contentEquals(word)) {
				text=str.replace(word, " *** ");
			}
		}

		return text;
	}


	//내부클래스
	class MultiServerT extends Thread{

		//멤버변수
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		boolean setWhisper=false;
		boolean setQuiet = false;
		String toWhisper=" ";
		String toQuiet=" ";


		//생성자 : Socket을 기반으로 입출력 스트림을 생성한다.
		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(
						this.socket.getOutputStream(),true);
				in = new BufferedReader(new InputStreamReader
						(this.socket.getInputStream(),"UTF-8"));

			}
			catch (Exception e) {
				System.out.println("예외:"+e);
			}
		}

		@Override
		public void run() {
			//클라이언트로부터 전송된 "대화명"을 저장할 변수
			String name = "";
			//메세지 저장용 변수
			String s = "";

			try {
				//클라이언트의 이름을 읽어와서 저장
				name = in.readLine();
				name = URLDecoder.decode(name,"UTF-8");
				name = checkDuplicate(name);
				//접속한 클라이언트에게 새로운 사용자의 입장을 알림.
				//접속자를 제외한 나머지 클라이언트만 입장메세지를 받는다.
				sendAllMsg("",name+ "님이 입장하셨습니다.");

				//현재 접속한 클라이언트를 HashMap에 저장한다.
				clientMap.put(name, out);

				//HashMap에 저장된 객체의 수로 접속자수를 파악할수 있다.
				System.out.println(name+" 접속");
				System.out.println(
						"현재 접속자 수는"+clientMap.size()+"명 입니다.");
				//입력한 메세지는 모든 클라이언트에게 Echo된다.
				//클라이언트의 메세지를 읽어온 후 콘솔에 출력하고 echo한다.
				while(in!=null) {

					s = in.readLine();
					s = URLDecoder.decode(s,"UTF-8");

					//금칙어
					s=banWords(s);

					if(s==null) {
						break;
					}


					//**db처리는 여기서 진행
					String query = "INSERT INTO chat_tb VALUES (seq_chat.nextval,?,?, sysdate) ";

					psmt = con.prepareStatement(query);

					psmt.setString(1, name);
					psmt.setString(2, s);

					psmt.executeUpdate();
					//읽어온 메세지를 콘솔에 출력하고...
					System.out.println(name+ " >> " + s);
					//클라이언트에게 Echo해준다.
					sendAllMsg(name,s);

				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				/*
				클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로
				넘어오게된다. 이때 "대화명"을 통해 remove()시켜준다.
				 */
				clientMap.remove(name);
				sendAllMsg("",name+"님이 퇴장하셨습니다.");
				//퇴장하는 클라이언트의 쓰레드명을 보여준다.
				System.out.println(name + " ["+
						Thread.currentThread().getName()+ "] 퇴장");
				System.out.println("현재 접속자 수는"+clientMap.size()+"명 입니다.");

				try {
					in.close();
					out.close();
					socket.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public class Commands extends MultiServerT{
			String[] msgArr;
			String commander;
			String order;
			String toName="";
			StringBuffer msg=new StringBuffer("");

			public Commands(Socket socket) {
				super(socket);
			}
			public Commands(String name, String fullmsg, Socket socket) {
				super(socket);
				commander=name;
				msgArr=fullmsg.split("");
				order=msgArr[0];
				toName=(msgArr.length>=2) ? msgArr[1] : " ";

				//공백으로 쪼개진 스트링 배열의 메세지 부분을 다시 합침
				for(int i=2; i<msgArr.length ; i++) {
					msg=msg.append(" "+msgArr[i]);
				}

				switch(order) {
				case"/list":
					showList(); break;

				case"/to":
					whisper(); break;//귓속말 셋팅, 메시지 입력시 split실행됨

				case"/quiet":
					quiet();   break;

				default:
					System.out.println("잘못된 명령어입니다.");
					System.out.println("/List : 접속자 리스트 보기");
					System.out.println("/to [이름] [메세지] : 귓속말보내기");
					System.out.println("/to [이름] : 귓속말 설정 고정/해제");
					break;
				}
			}

			void showList() {
				Iterator<String> it=clientMap.keySet().iterator();
				while(it.hasNext()) {
					out.println(it.next());
				}
			}

			void whisper() {
				if(msg.toString().equals(" ")) {
					if(setWhisper==true) {
						setWhisper=false;
						clientMap.get(commander).println(toName+" 에게 귓속말 고정 해제");
					}
					else if(setWhisper==false) {
						setWhisper=true;
						clientMap.get(commander).println(toName+" 에게 귓속말 고정 설정");
					}

				}
				else {
					clientMap.get(toName).println("["+commander+"] : " + msg);
				}
			}

			void quiet() {

				Iterator<String> it=clientMap.keySet().iterator();
				while(it.hasNext()) {

					String who=it.next();
					PrintWriter it_out = (PrintWriter)clientMap.get(who);

					if(toName.equals(who) && msg.toString().equals("")) {
						setQuiet = setQuiet ? false:true;
					}

					if (commander.equals(who)) {
						it_out.println(msg);
					}
					else if(toName.equals(who)) {
						it_out.println("["+who+"]: ****");
					}
					else {
						it_out.println("["+commander+"]:" + msg);
					}
				}
			}



		}
	}
}
