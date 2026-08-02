package com.anyservice.user.repository;

import com.anyservice.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // O Spring Data JPA cria a query SQL automaticamente baseado no nome do método!
    Optional<User> findByEmail(String email);
    
    boolean existsByUsernameIdentifier(String usernameIdentifier);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
