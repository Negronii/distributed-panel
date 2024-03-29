package com.ruiming.comp90015asmt2.Messages;

public class RefuseRequestMessage extends Message {
    public String username;
    public RefuseRequestMessage(String sender, String username) {
        super(sender);
        this.username = username;
    }

    @Override
    public String toString() {
        return "refuse," + sender + "," + username;
    }
}
