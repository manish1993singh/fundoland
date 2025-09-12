package com.example.user.repository;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

import com.example.user.entity.User;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
		Optional<User> findByEmail(String email);

		Iterable<User> findAllByDeletedTrue();

}