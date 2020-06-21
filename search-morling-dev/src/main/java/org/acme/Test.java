package org.acme;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Test {

    public static void main(String[] args) throws Exception {
        String name = new String("bob");
        Person bob = new Person();
        bob.name = name;

        Person deser = serializeDeserialize(bob);

        String name2 = new String("bob");
        Person bob2 = new Person();
        bob2.name = name2;

        Person deser2 = serializeDeserialize(bob2);

System.out.println();

    }

    private static Person serializeDeserialize(Person bob) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileOutputStream fileOutputStream
        = new FileOutputStream("yourfile.txt");
      ObjectOutputStream objectOutputStream
        = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(bob);
      objectOutputStream.flush();
      objectOutputStream.close();

      FileInputStream fileInputStream
        = new FileInputStream("yourfile.txt");
      ObjectInputStream objectInputStream
        = new ObjectInputStream(fileInputStream);
      Person p2 = (Person) objectInputStream.readObject();
      objectInputStream.close();

      return p2;
    }

    public static class Person implements Serializable {
        String name;
    }
}
