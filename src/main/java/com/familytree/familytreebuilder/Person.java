
package com.familytree.familytreebuilder;

public class Person {
    String name;
    Person parent;
    Person firstChild;
    Person nextSibling;

    public Person(String name) {
        this.name = name;
        this.parent = null;
        this.firstChild = null;
        this.nextSibling = null;
    }

    public String getName() {
        return name;
    }

    public Person getParent() {
        return parent;
    }

    public Person getFirstChild() {
        return firstChild;
    }

    public Person getNextSibling() {
        return nextSibling;
    }
}
