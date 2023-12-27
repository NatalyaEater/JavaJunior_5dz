package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {

    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            sentAllMessage("Server: " + name + " в сети.");
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String nameReceiver;
        String massageFromClient;

        while (socket.isConnected()) {
            try {
                nameReceiver = bufferedReader.readLine();
                massageFromClient = bufferedReader.readLine();
                broadcastMessage(nameReceiver, massageFromClient);
            }
            catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }


    //если nameReceiver == всем, то отправить всем, иначе отправить только выбраному пользователю
    private void broadcastMessage(String nameReceiver, String message){
        if (nameReceiver.equals("всем")){
            sentAllMessage(message);
        }
        else {
            sendPrivateMessage(nameReceiver, message);
        }
    }

    // Отправка сообщений всем пользователям
    private void sentAllMessage(String message){
        for (ClientManager client: clients) {
            try {
                if (!client.name.equals(name)) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
            catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // личные сообщения
    private void sendPrivateMessage(String nameReceiver , String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.name.equals(nameReceiver)) {
                    client.bufferedWriter.write("Личное сообщение от " +  message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    // Информируем отправителя о том, что личное сообщение отправлено
                    bufferedWriter.write("Личное сообщение отправлено " + nameReceiver);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    return;
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
        // Если получатель не найден, информируем отправителя
        try {
            bufferedWriter.write("Ошибка: Пользователь " + nameReceiver + " не найден или не в сети.");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        sentAllMessage("Server: " + name + " покинул чат.");
    }

}
