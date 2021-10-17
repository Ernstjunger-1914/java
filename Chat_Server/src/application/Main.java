package application;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	public static ExecutorService threadPool;
	
	public static Vector<Client> clients=new Vector<Client>();	
	//고객을 관리해주는 배열(고객 배열)
	
	ServerSocket serverSocket;
	
	public void startServer(String IP, int port) {
		try {
			serverSocket=new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		Runnable thread=new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket=serverSocket.accept();
						clients.add(new Client(socket));
						
						System.out.println("[ 클라이언트 접속 ]"+socket.getRemoteSocketAddress()+" : "+Thread.currentThread().getName());
					} catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool=Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	public void stopServer() {
		try {
			Iterator<Client> iterator=clients.iterator();	//GUI 컬렉션을 가져오기 위한 것을 표준화한 것.
			while(iterator.hasNext()) {		//arr[i+1]이 존재할 경우
				Client client=iterator.next();		//arr[i+1]를 client에 넣음.
				client.socket.close();
				iterator.remove();
			}
			
			if(serverSocket!=null&&!serverSocket.isClosed()) {
				serverSocket.close();
			}
			
			if(threadPool!=null&&!threadPool.isShutdown()) {
				threadPool.shutdown();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root=new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea=new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 13));
		root.setCenter(textArea);
		
		Button toggleButton=new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		
		root.setBottom(toggleButton);
		
		String IP="127.0.0.1";	//본인 IP
		int port=10000;		//임의
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message=String.format("[ 서버 시작 ]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message=String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					
					toggleButton.setText("시작하기"); 
				});
			}
		});
		
		Scene scene=new Scene(root, 400, 400);
		
		primaryStage.setTitle("[ 채팅 서버 ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();    //screen draw
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
