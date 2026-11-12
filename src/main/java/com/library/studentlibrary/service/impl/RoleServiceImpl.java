package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.Role;
import com.library.studentlibrary.repository.RoleRepository;
import com.library.studentlibrary.service.RoleService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll(Sort.by("roleId"));
    }
}
