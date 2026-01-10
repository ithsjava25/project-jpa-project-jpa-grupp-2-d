package org.example.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

public class JPAUtil {

    private static final EntityManagerFactory emf;

    static {
        emf = Persistence.createEntityManagerFactory("myPU");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emf.isOpen()) {
                emf.close();
            }
        }));
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void inTransaction(Consumer<EntityManager> work) {
        EntityManager em = getEntityManager();
        try {
            var tx = em.getTransaction();
            tx.begin();
            work.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static <T> T inTransactionResult(Function<EntityManager, T> work) {
        EntityManager em = getEntityManager();
        try {
            var tx = em.getTransaction();
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}

