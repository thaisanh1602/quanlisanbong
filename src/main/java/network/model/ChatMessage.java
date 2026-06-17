package network.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        TEXT, EMOJI, FILE_DATA, STEGO_IMAGE,
        VOICE_CALL_REQUEST, VOICE_CALL_ACCEPT, VOICE_CALL_REJECT, VOICE_CALL_END, VOICE_DATA,
        VIDEO_CALL_REQUEST, VIDEO_CALL_ACCEPT, VIDEO_CALL_REJECT, VIDEO_CALL_END, VIDEO_FRAME,
        USERNAME, USER_JOINED, USER_LEFT, USER_LIST
    }

    private String sender;
    private String receiver; // Cho tin nhắn riêng (Private Message)
    private Type type;
    private String text;    // text content, filename, or emoji, or user list (comma separated)
    private byte[] data;    // file bytes, image bytes, audio/video chunk
    private String extra;   // extra metadata (IP for P2P, password hint, role, vv.)
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String sender, Type type) {
        this.sender = sender;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // Builder-style
    public ChatMessage text(String t)       { this.text = t;       return this; }
    public ChatMessage data(byte[] d)       { this.data = d;       return this; }
    public ChatMessage extra(String e)      { this.extra = e;      return this; }
    public ChatMessage receiver(String r)   { this.receiver = r;   return this; }

    // Getters
    public String  getSender()    { return sender; }
    public String  getReceiver()  { return receiver; }
    public Type    getType()      { return type; }
    public String  getText()      { return text; }
    public byte[]  getData()      { return data; }
    public String  getExtra()     { return extra; }
    public long    getTimestamp() { return timestamp; }

    // Setters
    public void setSender(String sender)       { this.sender = sender; }
    public void setReceiver(String receiver)   { this.receiver = receiver; }
    public void setType(Type type)             { this.type = type; }
    public void setText(String text)           { this.text = text; }
    public void setData(byte[] data)           { this.data = data; }
    public void setExtra(String extra)         { this.extra = extra; }
    public void setTimestamp(long timestamp)   { this.timestamp = timestamp; }
}
