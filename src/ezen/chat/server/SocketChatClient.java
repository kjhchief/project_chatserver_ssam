package ezen.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * 접속한 클라이언트와 1:1로 통신하는 역할의 스레드
 * @author 김기정
 * @Date   2023. 2. 7.
 */
public class SocketChatClient extends Thread {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String clientIp;
	private String clientNickName;
	
	private ChatServer chatServer;
	
	public SocketChatClient(Socket socket, ChatServer chatServer) {
		try {
			this.socket = socket;
			this.chatServer = chatServer;
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			clientIp = socket.getInetAddress().getHostAddress();
		} catch (IOException e) {}
	}
	
	public String getClientIp() {
		return clientIp;
	}

	public String getClientNickName() {
		return clientNickName;
	}

	// 클라이언트 메시지 수신. 여기서 클라가 보낸걸 받아서 뿌리는데, 전체에게 뿌리기도 하고 특정 클라에게 뿌리기도 함.
	public void receiveMessage() {
		try {
			while (true) {
				String clientMessage = in.readUTF();
				System.out.println("[클라이언트]로부터 수신한 메시지 : " + clientMessage);
				//"CONNECT★방그리"
				String[] tokens = clientMessage.split("★");
				String messageType = tokens[0];
				switch (messageType) {
					// 최초 입장
					case "CONNECT":
						clientNickName = tokens[1];
						chatServer.addSocketChatClient(this);
						// 자기 포함해서 현재 접속한 모든 클라이언트에게 메시지 전송
						chatServer.sendAllMessage(clientMessage);
						
						// 현재 접속한 모든 클라언트의 대화명 목록 전송
						String nickNameList = chatServer.getNicknameList();
						// USER_LIST★admin★방그리,날라리,박찬울,김현아
						chatServer.sendAllMessage("USER_LIST★admin★"+nickNameList);
						break;
					// 채팅 메시지
					case "CHAT_MESSAGE":
						// 여기서 디엠이랑 전체챗을 분기 해볼까. 
						// 디엠
						String senderNickName = tokens[1];
						
						 
						// 자기 포함해서 현재 접속한 모든 클라이언트에게 메시지 전송
						chatServer.sendAllMessage(clientMessage);
						break;
					// 연결종료 메시지
						
					case "DM":
						String receiveNickName = getClientNickName(); 
						receiveNickName = tokens[1];
						chatServer.sendDirectMessage(receiveNickName, clientMessage);
						
					case "DIS_CONNECT":
						chatServer.removeSocketChatClient(this);
						chatServer.sendAllMessage(clientMessage);
						return;
				}
			}
		} catch (IOException e) {} 
		finally {
			System.out.println("[클라이언트(" + socket.getInetAddress().getHostAddress() + ")] 연결 종료함...");
		}
	}
	
	// 클라이언트에게 메시지 전송
	public void sendMessage(String message) {
		try {
			out.writeUTF(message);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 연결 종료
	public void close() {
		try {
			if(socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 스레드의 실행 진입점
	@Override
	public void run() {
		receiveMessage();
	}

}
