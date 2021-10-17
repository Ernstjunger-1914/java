package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	Socket socket;
	
	public Client(Socket socket) {
		this.socket=socket;
		receive();
	}
	
	//client에게 메세지 전달 받는 메소드
	public void receive() {		//kakaotalk에서 사용
		//서버에서 지속적으로 클라이언트가 채팅 전송여부를 확인
		Runnable thread=new Runnable() {
			
			@Override
			public void run() {
				try {
					while(true) {	//클라이언트로부터 받을 채팅이 있는지 지속적으로 확인
						InputStream in=socket.getInputStream();
						byte[] buffer=new byte[512];
						int length=in.read(buffer);
						
						if(length==-1) {
							throw new IOException();
						}
						
						System.out.println("[ 메세지 수신됨 ]"+socket.getRemoteSocketAddress()+" : "+Thread.currentThread().getName());
						String message=new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
				} catch (Exception e) {
					try {
						System.out.println("[ 메세지 수신 오류 ]"+socket.getRemoteSocketAddress()+" : "+Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		Main.threadPool.submit(thread);
		
	}
	
	//client에게 메세지를 전달하는 메소드
	public void send(String message) {
		Runnable thread=new Runnable() {
			
			@Override
			public void run() {
				try {
					OutputStream out=socket.getOutputStream();
					byte[] buffer=message.getBytes("UTF-8");
					out.write(buffer);	//버퍼에 담긴 내용을 서버에서 클라이언트로 전달
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[ 메세지 송신 오류 ]"+socket.getRemoteSocketAddress()+" : "+Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						e.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}
