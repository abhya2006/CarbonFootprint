package com.ecotrack.repository;

import com.ecotrack.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
    Optional<OrganizationMember> findFirstByUserEmail(String email);
    List<OrganizationMember> findByOrganizationId(Long organizationId);
}