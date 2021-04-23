package com.api.Autonova.repository;



import com.api.Autonova.models.site.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    List<User> findAllByMaster(boolean master);

    User findUserById(int id);

    User findUserByUsernameAndStatus(String username, boolean status);

    User findUserByToken(String token);


    User findUsersByUsername(String username);

    @Transactional
    void deleteById(int id);
}
