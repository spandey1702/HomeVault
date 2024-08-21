package com.mycompany.homevault.repo;
import com.mycompany.homevault.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

public class UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }
    }

    @Transactional
    public User findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    @Transactional
    public User findById(Integer id) {
        return entityManager.find(User.class, id);
    }

   
    }

