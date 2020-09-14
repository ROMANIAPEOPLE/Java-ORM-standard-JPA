package hellojpa;

import javax.persistence.*;

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx =em.getTransaction();
        tx.begin();

        try{

            Member member =em.find(Member.class,150L);
            member.setName("AAAAA");

            //detach는 영속성으로 더이상 관리하지 않겠다는 뜻으로,
            //JPA가 더이상 관리하지 않음. update등등 불가능
            em.detach(member);



            tx.commit();
        }catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();


    }
}
