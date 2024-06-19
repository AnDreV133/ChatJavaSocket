package newp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class FieldWindow extends Thread {
    private NetClientThread netClientThread; // поток клиента
    String nickname; // имя пользователя

    public FieldWindow() {
        // первым делом инициализируем стартовое меню
        initStartFrame();
    }

    public void initStartFrame() {
        // создаем фрейм
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());

        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        JLabel labelUsername = new JLabel("Ник: ");
        frame.add(labelUsername);

        TextField textFieldUsername = new TextField();
        textFieldUsername.setColumns(20);
        frame.add(textFieldUsername);

        // кнопка инициализирует клиента и переносит на окно чата
        Button buttonStartChatting = new Button("Начать общение!");
        buttonStartChatting.addActionListener((e) -> {
            nickname = textFieldUsername.getText();
            netClientThread = new NetClientThread("localhost", 8030);
            initChatFrame();
            frame.dispose();
        });
        frame.add(buttonStartChatting);
    }

    public void initChatFrame() {
        // создам пользователя на сервере
        netClientThread.postMessage("/setusername " + nickname);
        // создаем окно чата
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // на закрытие окна клиента закры
                netClientThread.postMessage("/exit");
                e.getWindow().dispose();
            }
        });

        JLabel labelUsername = new JLabel("Вы: " + nickname);
        frame.add(labelUsername, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(textArea, BorderLayout.CENTER);

        JTextField textFieldMessage = new JTextField();
        textFieldMessage.setColumns(30);

        JButton buttonSend = new JButton("->");
        buttonSend.addActionListener((e) -> {
            netClientThread.postMessage(textFieldMessage.getText());
            textFieldMessage.setText("");
        });
        Button buttonExit = new Button("Выйти");
        buttonExit.addActionListener((e) -> {
            netClientThread.postMessage("/exit");
            frame.dispose();
        });

        Container containerBorderLayout = new Container();
        containerBorderLayout.setLayout(new BorderLayout());
        containerBorderLayout.add(textFieldMessage, BorderLayout.WEST);
        containerBorderLayout.add(buttonSend, BorderLayout.EAST);
        containerBorderLayout.add(buttonExit, BorderLayout.SOUTH);
        frame.add(containerBorderLayout, BorderLayout.SOUTH);

        // поток для обработки событий чата
        new Thread(() -> {
            while (true) {
                String message = null;
                try {
                    message = netClientThread.waitMessage();
                } catch (IOException e) {
                    return;
                }

                if (message != null) {
                    textArea.append(message + "\n");
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        // старт программы клиента
        new FieldWindow();
    }
}