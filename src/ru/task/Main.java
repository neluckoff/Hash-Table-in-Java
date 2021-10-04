package ru.task;

import java.io.*;
import java.util.Scanner;

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
        System.out.println("Enter <NUMBER> <ADRESS>");
        String addNum = in.next();
        String addAdress = in.next();
        table.insert(addNum, addAdress);
        System.out.println("Complete!");
        table.printHashTable();

        //Поиск
        System.out.printf("\nEnter the number you want to find: ");
        String searchNum = in.next();
        System.out.printf(table.getTelefoNumber(searchNum).toString());

        //Удаление
        System.out.println("\nEnter the number you want remove: ");
        String removeNum = in.next();
        table.removeFunc(removeNum);
        System.out.println("Complete!");
        table.printHashTable();

        //Время чтения
        System.out.println("\nTesting Reading time");
        long time = System.currentTimeMillis();
        table.getTelefoNumber("89266715863");
        System.out.println("Time is: " + (System.currentTimeMillis() - time) + "ms");

        //Считываем с файла
        long time2 = System.currentTimeMillis();
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("SiAOD_Test.txt"));
        HashTable tableNew = (HashTable) inputStream.readObject();
        System.out.println("\nFrom the File: ");
        tableNew.printHashTable();
        System.out.println("Time is: " + (System.currentTimeMillis() - time2) + "ms");

        //Тест рехеширования
        table = new HashTable(1);
        System.out.println("\nTesting Reheshing");
        //Создал таблицу на 1 элемент и добавил туда 3 элемента
        table.insert("89023482402", "Domodedovo");
        table.insert("89230301020", "Sheremet'evo");
        table.insert("89991301230", "Vnukovo");
        //Все элементы были добавлены
        table.printHashTable();
    }
}

class ValueEntry implements Serializable  {
    String telefonNumber; //key
    String adress;

    ValueEntry(String telefonNumber, String adress) {
        this.telefonNumber = telefonNumber;
        this.adress = adress;
    }

    public ValueEntry clone() {
        return new ValueEntry(telefonNumber, adress);
    }

    @Override
    public String toString() {
        return "PHONE: " + telefonNumber + " | ADRESS: " + adress;
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
            outputStream = new ObjectOutputStream(new FileOutputStream("SiAOD_Test.txt"));
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
            hashing1 += hashing2;
            hashing1 %= hashSize;
        }

        table[hashing1] = new ValueEntry(telefonNumber, adress);
        size++;
        saveFile();
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