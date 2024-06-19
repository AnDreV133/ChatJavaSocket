package newp;

import java.net.*;
import java.io.*;
import java.util.Scanner;

class NetClientThread {
    private Socket socket; // ����� ��� ����������
    private BufferedReader in; // ����� ��� �����
    private BufferedWriter out; // ����� ��� ������
    private String nickname; // ��� ������������

    public NetClientThread(String addr, int port) {
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            new WriteMsg().start(); // ��������� ����� ��� �������� ���������
        } catch (IOException e) {
            NetClientThread.this.downService();
        }
    }

    public String waitMessage() throws IOException {
        return in.readLine(); // ������� ��������� �� �������
    }

    private void downService() {
        // ��������� ����������
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
    }

    private volatile String newMessage; // ���������� ��� �������� ����� ���������

    public void postMessage(String msg) {
        newMessage = msg;
    }

    public class WriteMsg extends Thread {
        @Override
        public void run() {
            // ����� ��� �������� ���������
            while (true) {
                String userWord;
                try {
                    if (newMessage == null) {
                        continue;
                    }

                    userWord = newMessage;
                    newMessage = null;

                    // ��������� ��� �������� ������ � ���������
                    if (userWord.equals("/exit")) {
                        out.write("#" + nickname + "\n");
                        out.flush();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {
                        }
                        out.write("/exit");
                        out.flush();
                        NetClientThread.this.downService();
                        break;
                    } else if (userWord.startsWith("/setusername")) {
                        String rawUsername = userWord.substring("/setusername".length() + 1);
                        if (nickname == null) {
                            nickname = rawUsername;
                            out.write("@" + nickname + "\n");
                        }
                    } else {
                        out.write(nickname + ": " + userWord + "\n");
                    }

                    out.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                    NetClientThread.this.downService();
                }
            }
        }
    }

    public static void main(String[] args) {
        // ������ � ���������� ������
        NetClientThread client = new NetClientThread("localhost", 8030);
        while (true) {
            client.postMessage(new Scanner(System.in).nextLine());
        }
    }
}