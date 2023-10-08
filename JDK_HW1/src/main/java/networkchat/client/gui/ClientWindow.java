package networkchat.client.gui;

import networkchat.client.common.ClientController;
import networkchat.client.common.ConnectData;
import networkchat.share.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class ClientWindow extends JFrame implements ActionListener {
    private final int WINDOW_HEIGHT = 545;
    private final int WINDOW_WIDTH = 380;

    ClientController controller;

    private final JTextField inputIp = new JTextField("localhost", 10);
    private final JTextField inputPort = new JTextField("9190");
    private final JTextField inputLogin = new JTextField("login");
    private final JPasswordField inputPassword = new JPasswordField("password");
    private final JButton btConnect = new JButton("Соединение");

    private final JTextArea log = new JTextArea(23, 30);

    private final JTextArea users = new JTextArea(22, 10);

    private final JTextArea message = new JTextArea(3, 22);
    private final JButton btSend = new JButton("Отправить");
    JPanel center;
    Boolean isOffline = true;
    Logger logger;

    public ClientWindow(ClientController controller, Logger logger) {
        super("Chat client");
        this.controller = controller;
        this.logger = logger;
        Image iconImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon-client.png"))).getImage();
        setIconImage(iconImage);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - WINDOW_WIDTH) / 2;
        int y = (screenSize.height - WINDOW_HEIGHT) / 2;
        setLocation(x, y);
        setResizable(false);
        setBackground(Color.lightGray);

        // настройка элементов окна
        inputPassword.setEchoChar('*');
        inputLogin.setToolTipText("аккаунт");
        inputPassword.setToolTipText("пароль");
        inputIp.setToolTipText("адрес сервера (URL или ip address)");
        inputPort.setToolTipText("порт сервера (1-65535)");
        log.setEditable(false);   // запрещаем ввод данных с клавиатуры


        // добавление обработчиков событий
        btConnect.addActionListener(this);
        btSend.addActionListener(this);
        message.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String msg = message.getText();
                    controller.btSend(msg);
                    message.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new GridLayout(2, 3));
        header.setBackground(Color.lightGray);
        header.setMaximumSize(new Dimension(368, 25));

        center = new JPanel(new FlowLayout());
        center.setPreferredSize(new Dimension(368, 420));

        JPanel footer = new JPanel(new FlowLayout());
        footer.setBackground(Color.lightGray);
        center.setMaximumSize(new Dimension(368, 28));


        header.add(inputIp);
        header.add(inputPort);
        header.add(Box.createHorizontalStrut(10));
        header.add(inputLogin);
        header.add(inputPassword);
        header.add(btConnect);

        JScrollPane scrollLog = new JScrollPane(log);
        scrollLog.setAutoscrolls(true);
        center.add(scrollLog);


        JScrollPane scrollMessage = new JScrollPane(message);
        scrollMessage.setMaximumSize(new Dimension(125, Integer.MAX_VALUE));
        footer.add(scrollMessage);
        footer.add(btSend);


        mainPanel.add(header);
        mainPanel.add(center);
        mainPanel.add(footer);
        add(mainPanel);
//        pack();
        setVisible(true);

        // подгружаем старые логи
        log.append(logger.getLogContents());
        log.setCaretPosition(log.getDocument().getLength()); //принудительный переход на последнюю строку поля
    }

    // обработка нажатия кнопок
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btSend) {
            // отправка сообщения
            String msg = message.getText();
            controller.btSend(msg);
            message.setText("");
        } else if (source == btConnect) {
            // соединение с сервером
            if (isOffline) {
                try {
                    controller.connect(getConnectInfo());
                } catch (InputException ex) {
                    switch (ex.getErrorCode()) {
                        case 1 -> out("Ошибка в поле Аккаунт");
                        case 2 -> out("Ошибка в пале Пароль");
                        case 3 -> out("Ошибка в пале Адрес сервера");
                        case 4 -> out("Ошибка в пале Порт");
                    }
                }
            } else {
                controller.disconnect();
            }
        }
    }

    private ConnectData getConnectInfo() throws InputException {
        String login, password, address;
        int port;
        try {
            login = inputLogin.getText();
            if (login.isEmpty()) throw new InputException(1);
            password = Arrays.toString(inputPassword.getPassword());
            if (password.isEmpty()) throw new InputException(2);
            address = inputIp.getText();
            if (address.isEmpty()) throw new InputException(3);
            port = Integer.parseInt(inputPort.getText());
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new InputException(4);
        }
        return new ConnectData(address, port, login, password);

    }

    public void out(String msg) {
        // Генерируем строку с текущим временем и сообщением
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedMessage = "[" + timeFormat.format(new Date()) + "] " + msg + "\n";
        log.append(formattedMessage);
        log.setCaretPosition(log.getDocument().getLength());
        logger.put(msg);
    }

    public void setOnlineTheme() {
        btConnect.setText("Отключить");
        inputIp.setEnabled(false);
        inputPassword.setEnabled(false);
        inputPort.setEnabled(false);
        inputLogin.setEnabled(false);
        btSend.setEnabled(true);
        message.setEnabled(true);
        center.setBackground(Color.green);
        isOffline = false;
    }

    public void setOfflineTheme() {
        btConnect.setText("Подключить");
        inputIp.setEnabled(true);
        inputPassword.setEnabled(true);
        inputPort.setEnabled(true);
        inputLogin.setEnabled(true);
        btSend.setEnabled(false);
        message.setEnabled(false);
        center.setBackground(Color.lightGray);
        isOffline = true;
    }


}
