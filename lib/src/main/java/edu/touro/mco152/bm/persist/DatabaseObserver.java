package edu.touro.mco152.bm.persist;

import edu.touro.mco152.bm.ObserverPattern.IObserve;
import jakarta.persistence.EntityManager;
 /*
  *Saves the persist info about write or read BM Run (e.g. into Derby Database)
  */
public class DatabaseObserver implements IObserve {
    EntityManager em;
    @Override
    public void update(DiskRun dr) {
        em = EM.getEntityManager();
        em.getTransaction().begin();
        em.persist(dr);
        em.getTransaction().commit();
    }
}
