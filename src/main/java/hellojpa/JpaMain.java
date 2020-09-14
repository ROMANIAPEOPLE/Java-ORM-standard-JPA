package hellojpa;

import javax.persistence.*;

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx =em.getTransaction();
        tx.begin();

        try{

            Member member1 = new Member(150L, "A");
            Member member2 = new Member(160L, "B");

            //영속성 컨텍스트에 쌓아두고,
            em.persist(member1);
            em.persist(member2);

            //커밋 시점에 SQL문이 실행된다.
            tx.commit();
        }catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();


    }
}
