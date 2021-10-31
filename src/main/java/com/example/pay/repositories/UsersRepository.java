package com.example.pay.repositories;

import com.example.pay.models.Users;
import org.springframework.data.repository.CrudRepository;

public interface UsersRepository extends CrudRepository<Users, Long> { }
