package newp;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

class Incoming extends Thread {
    private Socket socket; // ����� ��� �����������
    private BufferedReader in; // ����� ������ �� ������
    private BufferedWriter out; // ����� ������� � �����

    public Incoming(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // ����� ������ ��� ������ ���������� ������������
        start();
    }

    @Override
    public void run() {
        // ����� ������� ��� ���������� ������������� ����
        String word;
        try {
            try {
                while (true) {
                    word = in.readLine();
                    if (word.equals("/exit")) {
                        this.downService();
                        break;
                    }
                    System.out.println("Echoing: " + word);
                    for (Incoming vr : NetServerThread.serverList) {
                        vr.send(word);
                    }
                }
            } catch (NullPointerException ignored) {
            }

        } catch (IOException e) {
            this.downService();
        }
    }

    private void send(String msg) {
        // �������� �� ������ ������ ������
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {
        }

    }

    private void downService() {
        // �������� ������������ � �������
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (Incoming vr : NetServerThread.serverList) {
                    if (vr.equals(this)) vr.interrupt();
                    NetServerThread.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {
        }
    }
}

public class NetServerThread {

    public static final int PORT = 8030; // ���� �����������
    public static LinkedList<Incoming> serverList = new LinkedList<>(); // ������ �������������

    public static void main(String[] args) throws IOException {
        // �������� ������ � ������ ��� ���������� �������������
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server Started");
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    serverList.add(new Incoming(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}