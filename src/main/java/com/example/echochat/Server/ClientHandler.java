package com.example.echochat.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//каждый клиент имеет свой сокет
public class ClientHandler {
    private Socket socket;
    private ChatServer server;//этот класс знает все о клиентах
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {// пусть команда аутинтефикации будет /auth login1 pass1(разделены пробелами)
        while (true) {
            try {
                final String message = in.readUTF();
                if (message.startsWith("/auth")) {
                    //должны разделить это сообщение по пробелам
                    final String[] split = message.split("\\p{Blank}+");
                    final String login = split[1];
                    final String password = split[2];
                    final String nick = authService.getNickByLoginAndPassword(login, password);
                    System.out.println("Ник " + nick);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " зашел в чат");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage(" Неверные логин или пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
            // e.printStackTrace();
        }
    }

    private void readMessage() {
        //должна быть проверка. Если сообщение начинается с /w... то получить должен пользователь с этим ником(2,34,00)
        //метод который возвращает ник, кому отправить сообщение
        try {
            while (true) {
                final String message = in.readUTF();
                if ("/end".equals(message)) {
                    break;
                }
                server.broadcast(nick + ": " + message);//метод разослать всем
                out.writeUTF(message);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public String getNick() {
        return nick;
    }
}
