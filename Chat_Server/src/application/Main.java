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
	//���� �������ִ� �迭(�� �迭)
	
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
						
						System.out.println("[ Ŭ���̾�Ʈ ���� ]"+socket.getRemoteSocketAddress()+" : "+Thread.currentThread().getName());
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
			Iterator<Client> iterator=clients.iterator();	//GUI �÷����� �������� ���� ���� ǥ��ȭ�� ��.
			while(iterator.hasNext()) {		//arr[i+1]�� ������ ���
				Client client=iterator.next();		//arr[i+1]�� client�� ����.
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
		textArea.setFont(new Font("�������", 13));
		root.setCenter(textArea);
		
		Button toggleButton=new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		
		root.setBottom(toggleButton);
		
		String IP="127.0.0.1";	//���� IP
		int port=10000;		//����
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message=String.format("[ ���� ���� ]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message=String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					
					toggleButton.setText("�����ϱ�"); 
				});
			}
		});
		
		Scene scene=new Scene(root, 400, 400);
		
		primaryStage.setTitle("[ ä�� ���� ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();    //screen draw
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
