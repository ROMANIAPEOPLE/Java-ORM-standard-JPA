package hellojpa;

import javax.persistence.*;

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx =em.getTransaction();
        tx.begin();

        try{

            //비영속 상태
            Member member = new Member();
            member.setId(100L);
            member.setName("HelloB");

            //영속 상태
            System.out.println(" === BEFORE ===");
            em.persist(member);
            System.out.println(" === AFTER ===");
            //여기까진 영속 상태로 변경만되고, DB에 저장되지는 않음

            tx.commit();
        }catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();


    }
}
