
package com.familytree.familytreebuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository
        extends JpaRepository<FamilyMember, Long> {

    Optional<FamilyMember> findByNameIgnoreCase(String name);

    List<FamilyMember> findAllByParentNameIgnoreCase(String parentName);
}
