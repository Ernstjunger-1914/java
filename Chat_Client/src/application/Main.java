package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	
	public void startClient(String IP, int port) {
		Thread thread=new Thread() {
			public void run() {
				try {
					socket=new Socket(IP, port);
					receive();
				} catch (Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[ 접속 실패 ]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	public void stopClient() {
		try {
			if(socket!=null&&!socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		while(true) {
			try {
				InputStream in=socket.getInputStream();
				byte[] buffer=new byte[512];
				int length=in.read(buffer);
				
				if(length==-1) {
					throw new IOException();
				}
					String message=new String(buffer, 0, length, "UTF-8");
					Platform.runLater(() -> {
						textArea.appendText(message);
					});
						
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	public void send(String message) {
		Thread thread=new Thread() {
			public void run() {
				try {
					OutputStream out=socket.getOutputStream();
					byte[] buffer=message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root=new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox=new HBox();
		hbox.setSpacing(5);
		
		TextField userName=new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임 입력");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText=new TextField("127.0.0.1");
		TextField portText=new TextField("10000");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);
		
		textArea=new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input=new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText()+" : "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button sendButton=new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText()+" : "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionButton=new Button("접속하기");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("접속하기\n")) {
				int port=10000;
				try {
					port=Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[ 접속 ]\n");
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ 퇴장 ]\n");
				});
				connectionButton.setText("접속하기\n");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane=new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene=new Scene(root, 400, 400);
		primaryStage.setTitle("[ 채팅 클라이언트 ]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
