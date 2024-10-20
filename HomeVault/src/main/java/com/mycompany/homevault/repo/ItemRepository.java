package com.mycompany.homevault.repo;

import com.mycompany.homevault.model.Item;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
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

    public List<Item> findExpiringItemsForUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findExpiringItemsForUser'");
    }
}
