
package com.familytree.familytreebuilder;

public class FamilyMemberList {

    private static class Node {
        Person person;
        Node next;

        Node(Person person) {
            this.person = person;
            this.next = null;
        }
    }

    private Node head;
    private int size;

    public FamilyMemberList() {
        head = null;
        size = 0;
    }

    public boolean add(Person person) {
        if (person == null || find(person.getName()) != null) {
            return false;
        }

        Node newNode = new Node(person);

        if (head == null) {
            head = newNode;
        } else {
            Node current = head;

            while (current.next != null) {
                current = current.next;
            }

            current.next = newNode;
        }

        size++;
        return true;
    }

    public Person find(String name) {
        if (name == null) {
            return null;
        }

        Node current = head;

        while (current != null) {
            if (current.person.getName()
                    .equalsIgnoreCase(name.trim())) {
                return current.person;
            }

            current = current.next;
        }

        return null;
    }

    public boolean remove(String name) {
        if (name == null || head == null) {
            return false;
        }

        if (head.person.getName()
                .equalsIgnoreCase(name.trim())) {
            head = head.next;
            size--;
            return true;
        }

        Node current = head;

        while (current.next != null) {
            if (current.next.person.getName()
                    .equalsIgnoreCase(name.trim())) {
                current.next = current.next.next;
                size--;
                return true;
            }

            current = current.next;
        }

        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public Person getFirst() {
        return head == null ? null : head.person;
    }

    public Person getNext(Person person) {
        Node current = head;

        while (current != null) {
            if (current.person == person) {
                return current.next == null
                        ? null : current.next.person;
            }

            current = current.next;
        }

        return null;
    }
}
