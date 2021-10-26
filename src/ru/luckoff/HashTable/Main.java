package ru.luckoff.HashTable;

import java.io.*;
import java.util.Scanner;

import java.util.Arrays;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner in = new Scanner(System.in);

        //Создание таблицы
        HashTable table = new HashTable(5);
        table.insert("89266715863", "Moscow");
        table.insert("89934444449",  "St. Peterburg");
        table.insert("89231236412",  "Kazan");
        table.insert("88421674819",  "Novosibirsk");
        table.insert("80248124167",  "Vladivostok");
        table.printHashTable();

        //Добавление
        System.out.println("Введите <NUMBER> <ADRESS>");
        String addNum = in.next();
        String addAdress = in.next();
        table.insert(addNum, addAdress);
        System.out.println("Выполнено!");
        table.printHashTable();

        //Поиск
        System.out.printf("\nВведите номер, который вы хотите найти: ");
        String searchNum = in.next();
        System.out.printf(table.getTelefoNumber(searchNum).toString());

        //Удаление
        System.out.println("\nВведите номер, который вы хотите удалить: ");
        String removeNum = in.next();
        table.removeFunc(removeNum);
        System.out.println("Выполнено!");
        table.printHashTable();

        //Время чтения
        System.out.println("\nТестирование времени чтения");
        long time = System.currentTimeMillis();
        table.getTelefoNumber("89266715863");
        System.out.println("Time is: " + (System.currentTimeMillis() - time) + "ms");

        //Считываем с файла
        long time2 = System.currentTimeMillis();
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("Test.txt"));
        HashTable tableNew = (HashTable) inputStream.readObject();
        System.out.println("\nДостаем из файла: ");
        tableNew.printHashTable();
        System.out.println("Time is: " + (System.currentTimeMillis() - time2) + "ms");

        //Тест рехеширования
        table = new HashTable(1);
        System.out.println("\nТестирование рехеширования");
        //Создал таблицу на 1 элемент и добавил туда 3 элемента
        table.insert("89023482402", "Domodedovo");
        table.insert("89230301020", "Sheremet'evo");
        table.insert("89991301230", "Vnukovo");
        //Все элементы были добавлены
        table.printHashTable();

        //---------------------------------------------

        //Тест коллизии
        System.out.println("\nТестирование коллизий: попробуем добавить в таблицу 2 одинаковых ключа");
        HashTable table2 = new HashTable(2);
        table2.insert("89999999999", "Sheremet'evo");
        table2.insert("89999999999", "Vnukovo");

        //Достаем из файла по файловому номеру '2'
        System.out.println("\nТестирование доступа по файловому номеру: " +
                "добавим в таблицу 5 записей и достанем значение по номеру '3'");
        table2 = new HashTable(5);
        table2.insert("89999999991", "Sheremet'evo");
        table2.insert("89999999992", "Vnukovo");
        table2.insert("89999999993", "Domodevo");
        table2.insert("89999999994", "Vnukovo");
        table2.insert("89999999995", "Vnukovo");

        ObjectInputStream inputStream2 = new ObjectInputStream(new FileInputStream("Test.txt"));
        HashTable tableNew2 = (HashTable) inputStream2.readObject();
        ValueEntry valueEntry = tableNew2.getValueByFileNumber(3);
        System.out.println("Значение: " + valueEntry);

        //Удаление и получение по номеру
        System.out.println("\nПопробуем удалить значение из файла с номером '3' и снова получить");
        inputStream2 = new ObjectInputStream(new FileInputStream("Test.txt"));
        tableNew2 = (HashTable) inputStream2.readObject();
        tableNew2.removeValueByFileNumber(3);
        System.out.println("Удалили, пробуем получить");

        inputStream2 = new ObjectInputStream(new FileInputStream("Test.txt"));
        tableNew2 = (HashTable) inputStream2.readObject();
        valueEntry = tableNew2.getValueByFileNumber(3);
        System.out.println("Значение: " + valueEntry);
    }
}

class ValueEntry implements Serializable  {
    String telefonNumber;
    String adress;
    int fileNumber;

    ValueEntry(String telefonNumber, String adress) {
        this.telefonNumber = telefonNumber;
        this.adress = adress;
    }

    public ValueEntry clone() {
        return new ValueEntry(telefonNumber, adress);
    }

    @Override
    public String toString() {
        return "PHONE: " + telefonNumber + " | ADRESS: " + adress + " | FileNumber: " + fileNumber;
    }
}

class HashTable implements Serializable  {
    private int hashSize, size, totalSize;
    private ValueEntry[] table;

    public HashTable(int ts) {
        size = 0;
        hashSize = ts;
        table = new ValueEntry[hashSize];

        for (int i = 0; i < hashSize; i++)
            table[i] = null;
        totalSize = getPrime();
    }

    public ValueEntry getValueByFileNumber(int fileNumber) {
        return Arrays.stream(table).filter(Objects::nonNull).filter(v -> v.fileNumber == fileNumber).findFirst().orElse(null);
    }

    public void removeValueByFileNumber(int fileNumber) {
        for (int i = 0; i < table.length; i++) {
            if(table[i] != null) {
                if(table[i].fileNumber == fileNumber) {
                    table[i] = null;
                }
            }
        }
        saveFile();
    }

    public int getPrime() {
        for (int i = hashSize - 1; i >= 1; i--) {
            int cnt = 0;
            for (int j = 2; j * j <= i; j++)
                if (i % j == 0)
                    cnt++;
            if (cnt == 0)
                return i;
        }
        return 3;
    }

    public void saveFile() {
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream("Test.txt"));
            outputStream.writeObject(this);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ValueEntry getTelefoNumber(String telefonNumber) {  // getKey
        int hash1 = hasingOne(telefonNumber);
        int hash2 = hashingTwo(telefonNumber);

        while (table[hash1] != null && !table[hash1].telefonNumber.equals(telefonNumber)) {
            hash1 += hash2;
            hash1 %= hashSize;
        }
        return table[hash1];
    }

    public void insert(String telefonNumber, String adress) {
        if (size >= hashSize) {
            rehash();
        }

        int hashing1 = hasingOne(telefonNumber);
        int hashing2 = hashingTwo(telefonNumber);
        while (table[hashing1] != null) {
            int oldHash = hashing1;
            hashing1 += hashing2;
            hashing1 %= hashSize;

            //Только на случай тестирования коллизии!!!
//            System.out.println("При попытке добавления нового ключа " + telefonNumber +
//                    " произошла коллизия и хеш '" + oldHash +
//                    "' оказался занят. Сгенерировали новый: '" + hashing1 + "'");
        }

        int nextFileNumber = getNextFileNumber();
        table[hashing1] = new ValueEntry(telefonNumber, adress);
        table[hashing1].fileNumber = nextFileNumber;
        size++;
        saveFile();
    }

    public int getNextFileNumber() {
        return Arrays.stream(table).filter(Objects::nonNull).mapToInt(v -> v.fileNumber).max().orElse(0) + 1;
    }

    public void removeFunc(String telefonNumber) {
        int hash1 = hasingOne(telefonNumber);
        int hash2 = hashingTwo(telefonNumber);
        while (table[hash1] != null
                && !table[hash1].telefonNumber.equals(telefonNumber)) {
            hash1 += hash2;
            hash1 %= hashSize;
        }
        table[hash1] = null;
        size--;
        saveFile();
    }

    private int hasingOne(String y) {
        int myhashVal1 = y.hashCode();
        myhashVal1 %= hashSize;
        if (myhashVal1 < 0)
            myhashVal1 += hashSize;
        return myhashVal1;
    }

    private int hashingTwo(String y) {
        int myhashVal2 = y.hashCode();
        myhashVal2 %= hashSize;
        if (myhashVal2 < 0)
            myhashVal2 += hashSize;
        return totalSize - myhashVal2 % totalSize;
    }

    public void printHashTable() {
        System.out.println("\nHash Table");

        for (int i = 0; i < hashSize; i++)
            if (table[i] != null)
                System.out.println("PHONE: " + table[i].telefonNumber + " | ADRESS: " + table[i].adress);
    }

    private void rehash() {
        ValueEntry[] oldTable = table.clone();

        hashSize *= 2;
        table = new ValueEntry[hashSize];

        for (int i = 0; i < oldTable.length; i++) {
            int hashing1 = hasingOne(oldTable[i].telefonNumber);
            int hashing2 = hashingTwo(oldTable[i].telefonNumber);
            while (table[hashing1] != null) {
                hashing1 += hashing2;
                hashing1 %= hashSize;
            }
            table[hashing1] = oldTable[i].clone();
        }
    }
}