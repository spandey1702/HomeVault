package com.mycompany.homevault.repo;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mycompany.homevault.model.Document;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Repository
public class DocumentRepository {

    @Autowired
    private EntityManager entityManager;

    public List<Document> findAllDocuments() {
        String query = "SELECT d FROM Document d";
        TypedQuery<Document> typedQuery = entityManager.createQuery(query, Document.class);
        return typedQuery.getResultList();
    }

    public Document findById(Long id) {
        return entityManager.find(Document.class, id);
    }

    public Document findMaxIdDocument() {
        String query = "SELECT d FROM Document d WHERE d.id = (SELECT MAX(d2.id) FROM Document d2)";
        TypedQuery<Document> typedQuery = entityManager.createQuery(query, Document.class);
        return typedQuery.getSingleResult();
    }

    public void save(Document document) {
        entityManager.persist(document);
    }

    public List<Document> findDocumentsByUsername(String username) {
        String query = "SELECT d FROM Document d WHERE d.username = :username";
        TypedQuery<Document> typedQuery = entityManager.createQuery(query, Document.class);
        typedQuery.setParameter("username", username);
        return typedQuery.getResultList();
    }
}
