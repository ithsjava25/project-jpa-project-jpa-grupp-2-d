package org.example.util;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JPAUtilTest {

    @Test
    public void getEntityManager_shouldReturnOpenEntityManager() {
        EntityManager em = JPAUtil.getEntityManager();

        assertNotNull(em);
        assertTrue(em.isOpen());

        em.close();
    }

    @Test
    public void inTransaction_shouldExecuteWithoutException() {
        assertDoesNotThrow(() ->
            JPAUtil.inTransaction(em -> {}));
    }


}
