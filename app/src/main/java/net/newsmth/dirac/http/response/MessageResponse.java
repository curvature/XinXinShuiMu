package net.newsmth.dirac.http.response;

import net.newsmth.dirac.model.MessageSummary;

import java.util.List;

public class MessageResponse {

    private int error;

    private List<MessageSummary> messages;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public List<MessageSummary> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageSummary> messages) {
        this.messages = messages;
    }
}
