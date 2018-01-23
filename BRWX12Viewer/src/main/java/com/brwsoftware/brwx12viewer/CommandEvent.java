package com.brwsoftware.brwx12viewer;

import java.io.File;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class CommandEvent extends Event {

	private static final long serialVersionUID = 5150259704080490347L;

	public static final EventType<CommandEvent> COMMAND = new EventType<CommandEvent>(Event.ANY, "COMMAND");
    public static final EventType<CommandEvent> ANY = COMMAND;
    
    private String command;
    private File file;
    private String text;

    public CommandEvent(String command) {
        super(COMMAND);
        this.command = command;
    }

    public CommandEvent(String command, File file) {
        super(COMMAND);
        this.command = command;
        this.file = file;
    }

    public CommandEvent(String command, String text) {
        super(COMMAND);
        this.command = command;
        this.text = text;
    }
    
//    public CommandEvent(Object source, EventTarget target, String command) {
//        super(source, target, COMMAND);
//        this.command = command;
//    }
//
//    public CommandEvent(Object source, EventTarget target, String command, File file) {
//        super(source, target, COMMAND);
//        this.command = command;
//        this.file = file;
//    }
    
	@Override
    public CommandEvent copyFor(Object newSource, EventTarget newTarget) {
        return (CommandEvent) super.copyFor(newSource, newTarget);
    }

    @SuppressWarnings("unchecked")
	@Override
    public EventType<? extends CommandEvent> getEventType() {
        return (EventType<? extends CommandEvent>) super.getEventType();
    }

	public String getCommand() {
		return command;
	}

//	public void setCommand(String command) {
//		this.command = command;
//	}

	public File getFile() {
		return file;
	}

	public String getText() {
		return text;
	}

//	public void setText(String text) {
//		this.text = text;
//	}

//	public void setFile(File file) {
//		this.file = file;
//	}
}
