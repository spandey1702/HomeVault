package com.mycompany.homevault.repo;

import com.mycompany.homevault.model.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ItemRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Item item) {
        entityManager.persist(item);
    }

    public Item findById(Integer id) {
        return entityManager.find(Item.class, id);
    }

    @Transactional
    public void update(Item item) {
        entityManager.merge(item);
    }

    @Transactional
    public void delete(Item item) {
        entityManager.remove(item);
    }

    public List<Item> findByUsername(String username) {
        return entityManager.createQuery("SELECT i FROM Item i WHERE i.username = :username", Item.class)
                            .setParameter("username", username)
                            .getResultList();
    }
}
