
package com.familytree.familytreebuilder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyTreeService {

    private final FamilyMemberList members = new FamilyMemberList();
    private final FamilyMemberRepository repository;

    public FamilyTreeService(FamilyMemberRepository repository) {
        this.repository = repository;
    }

    // Load saved members when the application starts
    @PostConstruct
    public void loadMembersFromDatabase() {

        members.getFirst(); // Access the custom linked list

        for (FamilyMember saved : repository.findAll()) {
            if (saved.getName() != null
                    && members.find(saved.getName()) == null) {
                members.add(new Person(saved.getName()));
            }
        }

        // Restore parent-child relationships
        for (FamilyMember saved : repository.findAll()) {

            if (saved.getName() == null
                    || saved.getParentName() == null) {
                continue;
            }

            Person child = members.find(saved.getName());
            Person parent = members.find(saved.getParentName());

            if (child != null && parent != null
                    && child != parent
                    && child.parent == null
                    && !createsCycle(parent, child)) {

                attachParent(parent, child);
            }
        }
    }

    private boolean createsCycle(Person parent, Person child) {
        Person current = parent;

        while (current != null) {
            if (current == child) {
                return true;
            }
            current = current.parent;
        }

        return false;
    }

    private void attachParent(Person parent, Person child) {
        child.parent = parent;
        child.nextSibling = null;

        if (parent.firstChild == null) {
            parent.firstChild = child;
        } else {
            Person sibling = parent.firstChild;

            while (sibling.nextSibling != null) {
                sibling = sibling.nextSibling;
            }

            sibling.nextSibling = child;
        }
    }

    // Add a family member
    @Transactional
    public String addMember(String name) {

        if (name == null || name.trim().isEmpty()) {
            return "Please enter a member name.";
        }

        name = name.trim();

        if (members.find(name) != null
                || repository.findByNameIgnoreCase(name).isPresent()) {
            return "Member already exists: " + name;
        }

        Person person = new Person(name);
        members.add(person);

        repository.save(new FamilyMember(name, null));

        return "Member added successfully: " + name;
    }

    // Search a family member
    public Person searchMember(String name) {
        if (name == null) {
            return null;
        }

        return members.find(name.trim());
    }

    // Update a member's name
    @Transactional
    public String updateMember(String oldName, String newName) {

        if (oldName == null || oldName.trim().isEmpty()) {
            return "Please enter the current member name.";
        }

        if (newName == null || newName.trim().isEmpty()) {
            return "New name cannot be empty.";
        }

        oldName = oldName.trim();
        newName = newName.trim();

        Person person = members.find(oldName);

        if (person == null) {
            return "Member not found.";
        }

        Person duplicate = members.find(newName);

        if (duplicate != null && duplicate != person) {
            return "That name already exists.";
        }

        FamilyMember saved = repository
                .findByNameIgnoreCase(oldName)
                .orElse(null);

        if (saved == null) {
            return "Member not found in database.";
        }

        saved.setName(newName);
        repository.save(saved);

        for (FamilyMember child :
                repository.findAllByParentNameIgnoreCase(oldName)) {
            child.setParentName(newName);
            repository.save(child);
        }

        person.name = newName;

        return "Member updated successfully: " + newName;
    }

    // Delete a family member
    @Transactional
    public String deleteMember(String name) {

        if (name == null || name.trim().isEmpty()) {
            return "Please enter a member name.";
        }

        name = name.trim();

        Person person = members.find(name);

        if (person == null) {
            return "Member not found.";
        }

        FamilyMember saved = repository
                .findByNameIgnoreCase(name)
                .orElse(null);

        // Remove the member from the parent's child list
        if (person.parent != null) {

            Person current = person.parent.firstChild;
            Person previous = null;

            while (current != null && current != person) {
                previous = current;
                current = current.nextSibling;
            }

            if (current != null) {
                if (previous == null) {
                    person.parent.firstChild = current.nextSibling;
                } else {
                    previous.nextSibling = current.nextSibling;
                }
            }
        }

        // Make each child a separate root member
        Person child = person.firstChild;

        while (child != null) {
            Person next = child.nextSibling;

            child.parent = null;
            child.nextSibling = null;

            FamilyMember childRecord = repository
                    .findByNameIgnoreCase(child.name)
                    .orElse(null);

            if (childRecord != null) {
                childRecord.setParentName(null);
                repository.save(childRecord);
            }

            child = next;
        }

        person.parent = null;
        person.firstChild = null;
        person.nextSibling = null;

        members.remove(name);

        if (saved != null) {
            repository.delete(saved);
        }

        return "Member deleted successfully. Their children are now root members.";
    }

    // Assign a parent
    @Transactional
    public String setParent(String parentName, String childName) {

        if (parentName == null || childName == null
                || parentName.trim().isEmpty()
                || childName.trim().isEmpty()) {
            return "Please enter both parent and child names.";
        }

        parentName = parentName.trim();
        childName = childName.trim();

        Person parent = members.find(parentName);
        Person child = members.find(childName);

        if (parent == null || child == null) {
            return "Parent or child not found. Add both members first.";
        }

        if (parent == child) {
            return "A person cannot be their own parent.";
        }

        if (child.parent != null) {
            return "This member already has a parent.";
        }

        if (createsCycle(parent, child)) {
            return "Invalid relationship: cycle detected.";
        }

        FamilyMember childRecord = repository
                .findByNameIgnoreCase(child.name)
                .orElse(null);

        if (childRecord == null) {
            return "Child not found in database.";
        }

        attachParent(parent, child);

        childRecord.setParentName(parent.name);
        repository.save(childRecord);

        return parent.name + " is now the parent of " + child.name;
    }

    // Display the complete family tree
    public String getFamilyTree() {

        if (members.isEmpty()) {
            return "Family tree is empty.";
        }

        StringBuilder result = new StringBuilder();
        Person person = members.getFirst();

        while (person != null) {
            if (person.parent == null) {
                appendTree(person, "", result);
            }

            person = members.getNext(person);
        }

        return result.length() == 0
                ? "No root members found."
                : result.toString();
    }

    private void appendTree(
            Person person, String indent, StringBuilder result) {

        result.append(indent)
                .append("|-- ")
                .append(person.name)
                .append("\n");

        Person child = person.firstChild;

        while (child != null) {
            appendTree(child, indent + "    ", result);
            child = child.nextSibling;
        }
    }

    // Display siblings
    public String getSiblings(String name) {

        Person person = searchMember(name);

        if (person == null) {
            return "Member not found.";
        }

        if (person.parent == null) {
            return "No siblings found for this root member.";
        }

        StringBuilder result = new StringBuilder();
        Person current = person.parent.firstChild;

        while (current != null) {
            if (current != person) {
                result.append(current.name).append("\n");
            }

            current = current.nextSibling;
        }

        return result.length() == 0
                ? "No siblings found."
                : result.toString();
    }

    // Display ancestors
    public String getAncestors(String name) {

        Person person = searchMember(name);

        if (person == null) {
            return "Member not found.";
        }

        StringBuilder result = new StringBuilder();
        Person current = person.parent;

        while (current != null) {
            result.append(current.name).append("\n");
            current = current.parent;
        }

        return result.length() == 0
                ? "No ancestors found."
                : result.toString();
    }

    // Display descendants
    public String getDescendants(String name) {

        Person person = searchMember(name);

        if (person == null) {
            return "Member not found.";
        }

        StringBuilder result = new StringBuilder();
        appendDescendants(person.firstChild, result, 0);

        return result.length() == 0
                ? "No descendants found."
                : result.toString();
    }

    private void appendDescendants(
            Person person, StringBuilder result, int level) {

        Person current = person;

        while (current != null) {
            result.append("  ".repeat(level))
                    .append("|-- ")
                    .append(current.name)
                    .append("\n");

            appendDescendants(
                    current.firstChild, result, level + 1);

            current = current.nextSibling;
        }
    }

    // Count generations
    public int getGenerationCount() {

        int maxGeneration = 0;
        Person person = members.getFirst();

        while (person != null) {
            if (person.parent == null) {
                int generation = countGeneration(person, 1);

                if (generation > maxGeneration) {
                    maxGeneration = generation;
                }
            }

            person = members.getNext(person);
        }

        return maxGeneration;
    }

    private int countGeneration(Person person, int level) {

        int maxLevel = level;
        Person child = person.firstChild;

        while (child != null) {
            int childLevel = countGeneration(child, level + 1);

            if (childLevel > maxLevel) {
                maxLevel = childLevel;
            }

            child = child.nextSibling;
        }

        return maxLevel;
    }

    // Get total family members
    public int getMemberCount() {
        return members.size();
    }

    // Access the custom linked list
    public FamilyMemberList getMembers() {
        return members;
    }
}
