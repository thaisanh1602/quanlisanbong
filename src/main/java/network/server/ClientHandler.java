package network.server;

import network.model.ChatMessage;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username = "Unknown";
    private String role = "Unknown"; // "Admin" hoặc "Khach Hang"

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        // OOS must be created BEFORE OIS to avoid deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in  = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            ChatMessage msg;
            while ((msg = (ChatMessage) in.readObject()) != null) {
                handleMessage(msg);
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("[~] Client left: " + username);
        } catch (Exception e) {
            System.out.println("[!] Error [" + username + "]: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleMessage(ChatMessage msg) {
        System.out.printf("[%s] %s%n", msg.getType(), msg.getSender());
        switch (msg.getType()) {
            case USERNAME:
                username = msg.getSender();
                if (msg.getExtra() != null) role = msg.getExtra();
                ChatServer.broadcastUserList(); // Gửi danh sách user mới
                
                // Gửi tin nhắn offline chờ nhận cho user này
                for (ChatMessage offlineMsg : ChatServer.offlineMessages) {
                    if (offlineMsg.getReceiver() != null && offlineMsg.getReceiver().equalsIgnoreCase(username)) {
                        sendMessage(offlineMsg);
                        ChatServer.offlineMessages.remove(offlineMsg);
                    }
                }
                break;

            // Voice/video data – relay only to others to avoid echo
            case VOICE_DATA:
            case VIDEO_FRAME:
                ChatServer.broadcastExcept(msg, this);
                break;

            // All other messages
            default:
                if (msg.getReceiver() != null && !msg.getReceiver().isEmpty()) {
                    // Private Message
                    ChatServer.sendPrivateMessage(msg);
                } else {
                    // Cải tiến Broadcast: Khách Hàng không được nhận tin nhắn từ phòng chung trừ khi chính họ gửi
                    if (msg.getType() == ChatMessage.Type.TEXT || 
                        msg.getType() == ChatMessage.Type.EMOJI || 
                        msg.getType() == ChatMessage.Type.FILE_DATA || 
                        msg.getType() == ChatMessage.Type.STEGO_IMAGE) {
                        
                        for (ClientHandler c : ChatServer.clients) {
                            // Nếu người gửi là Khách Hàng, chỉ gửi tới chính Khách Hàng đó và Nhân Viên (không gửi Admin/Quan Ly)
                            if ("Khach Hang".equalsIgnoreCase(role)) {
                                if ("Nhan Vien".equalsIgnoreCase(c.getRole()) || "Nhân viên".equalsIgnoreCase(c.getRole()) || c == this) {
                                    c.sendMessage(msg);
                                }
                            } else {
                                // Nếu người gửi là Nhan Vien / Quan Ly, gửi cho tất cả Nhan Vien & Quan Ly
                                if (!"Khach Hang".equalsIgnoreCase(c.getRole())) {
                                    c.sendMessage(msg);
                                }
                            }
                        }
                    } else {
                        // System messages (USER_JOINED, USER_LEFT, USER_LIST, etc.)
                        ChatServer.broadcast(msg);
                    }
                }
                break;
        }
    }

    private void disconnect() {
        ChatServer.removeClient(this);
        if (!username.equals("Unknown")) {
            ChatServer.broadcastUserList();
        }
        try { if (!socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }

    public void sendMessage(ChatMessage msg) {
        try {
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
                out.reset(); // prevent object caching
            }
        } catch (IOException ignored) {}
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
}
