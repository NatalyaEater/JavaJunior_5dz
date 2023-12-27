package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        System.out.println("Start Client ...");

        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите своё имя: ");
            String name = scanner.nextLine();

            Socket socket = new Socket("localhost", 1300);
            Client client = new Client(socket, name);

            InetAddress inetAddress = socket.getInetAddress();
            System.out.println("IP адрес: " + inetAddress);
            String remoteIp = inetAddress.getHostAddress();
            System.out.println("Удаленный IP адрес: " + remoteIp);
            System.out.println("LocalPort: " + socket.getLocalPort());

            client.listenForMessage();
            client.sendMessage();

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}