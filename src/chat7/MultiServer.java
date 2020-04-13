package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {

	static ServerSocket serverSocket=null;
	static Socket socket=null;
	Map<String, PrintWriter> clientMap;

	public MultiServer() {
		clientMap=new HashMap<String, PrintWriter>();
		Collections.synchronizedMap(clientMap);
	}

	public void init() {

		try {
			serverSocket=new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			while(true) {
				socket=serverSocket.accept();
				Thread mst =new MultiServerT(socket);
				mst.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}

		}
	}
	public static void main(String[] args) {

		MultiServer ms=new MultiServer();
		ms.init();
	}

	public void sendAllMsg(String name, String msg) {

		Iterator<String> it=clientMap.keySet().iterator();

		while(it.hasNext()) {
			try {
				PrintWriter it_out=(PrintWriter)
						clientMap.get(it.next());

				if(name.equals("")) {
					it_out.println(msg);
				}
				else {
					it_out.println("["+name+"]:"+msg);
				}
			}
			catch(Exception e) {
				System.out.println("예외:" +e);
			}
		}

	}

	//내부클래스
	class MultiServerT extends Thread{

		//멤버변수
		Socket socket;
		PrintWriter out=null;
		BufferedReader in=null;

		//생성자: Socket을 기반으로 입출력 스트림을 생성한다
		public MultiServerT(Socket socket) {
			this.socket=socket;

			try {
				out=new PrintWriter(this.socket.getOutputStream(),
						true);
				in = new BufferedReader(new 
						InputStreamReader(this.socket.getInputStream()));
			}
			catch(Exception e) {
				System.out.println("예외:" +e);
			}

		}
		@Override
		public void run() {
			
			//클라이언트로부터 전송된 "대화명"을 저장할 변수
			String name="";
			//메세지 저장용 변수
			String s="";


			try {
				//클라이언트의 이름을 읽어와서 저장
				name=in.readLine();
				
				//접속한 클라이언트에게 새로운 사용자의 입장을 알림.
				//접속자를 제외한 나머지 클라이언트만 입장메세지를 받는다.
				sendAllMsg("", name+ "님이 입장하셨습니다.");

				//현재 접속한 클라이언트를 HashMap에 저장한다.
				clientMap.put(name, out);

				//HashMap에 저장된 객체의 수로 접속자수를 파악할 수 있다.
				System.out.println(name+" 접속 ");
				System.out.println("현재 접속자 수는" 
						+ clientMap.size()+"명 입니다.");

				//입력한 메세지는 모든 클라이언트에게 Echo 된다.
				
				while (in!=null) {
					s=in.readLine();
					if(s==null)
						break;
					System.out.println(name+">>"+s);
					sendAllMsg(name,s);

				}
			}
			catch (Exception e) {
				System.out.println("예외:" +e);
			}
			finally {
				/*
				 클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로
				 넘어오게된다. 이때 "대화명"을 통해 remove()시켜준다.
				 */
				clientMap.remove(name);
				sendAllMsg("", name+"님이 퇴장하셨습니다.");
				//퇴장하는 클라이언트의 Thread명을 보여준다.
				System.out.println(name+" [" +
						Thread.currentThread().getName()+"] 퇴장");
				System.out.println("현재 접속자 수는 "
						+clientMap.size()+"명 입니다.");

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
	}
}
