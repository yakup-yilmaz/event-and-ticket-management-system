package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Yakup Yılmaz
 */

@Repository
public interface UserRepository extends JpaRepository<User,Long> 
{
  boolean existsByEmail(String email);
  Optional<User> findByEmail(String email);
  
}
