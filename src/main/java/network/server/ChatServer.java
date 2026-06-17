package network.server;

import network.model.ChatMessage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    public static final int PORT = 5005;
    public static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public static final java.util.concurrent.CopyOnWriteArrayList<ChatMessage> offlineMessages = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║    JavaChat Server v2.0      ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  Port : " + PORT + "                  ║");
        System.out.println("╚══════════════════════════════╝");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Ready. Waiting for connections...\n");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[+] Connection from: " + socket.getInetAddress().getHostAddress());
                try {
                    ClientHandler handler = new ClientHandler(socket);
                    clients.add(handler);
                    handler.start();
                } catch (IOException e) {
                    System.out.println("[!] Failed to handle client: " + e.getMessage());
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Server failed: " + e.getMessage());
        }
    }

    /** Broadcast to ALL clients (including sender – server echoes back). */
    public static void broadcast(ChatMessage msg) {
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }

    /** Broadcast to all clients EXCEPT sender. */
    public static void broadcastExcept(ChatMessage msg, ClientHandler sender) {
        for (ClientHandler c : clients) {
            if (c != sender) c.sendMessage(msg);
        }
    }

    public static void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("[-] Client removed. Active: " + clients.size());
    }

    /** Gửi danh sách user đang online (kèm role) */
    public static void broadcastUserList() {
        StringBuilder sb = new StringBuilder();
        for (ClientHandler c : clients) {
            sb.append(c.getUsername()).append("|").append(c.getRole()).append(",");
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        
        ChatMessage listMsg = new ChatMessage("System", ChatMessage.Type.USER_LIST).text(sb.toString());
        broadcast(listMsg);
    }

    /** Gửi tin nhắn private tới đúng người nhận (và echo về cho người gửi) */
    public static void sendPrivateMessage(ChatMessage msg) {
        String receiver = msg.getReceiver();
        String sender = msg.getSender();
        boolean receiverOnline = false;
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(receiver)) {
                c.sendMessage(msg);
                receiverOnline = true;
            } else if (c.getUsername().equals(sender)) {
                c.sendMessage(msg);
            }
        }
        if (!receiverOnline) {
            offlineMessages.add(msg);
            System.out.println("[OFFLINE STORE] Saved offline private message from " + sender + " to " + receiver);
        }
    }
}
