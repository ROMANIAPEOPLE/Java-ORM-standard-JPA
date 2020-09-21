package hellojpa;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx =em.getTransaction();
        tx.begin();

        try{

            Member member = new Member();
            member.setUsername("member1");
            em.persist(member);

            TypedQuery<Member> query = em.createQuery("select m from Member m where m.username =:username",
                    Member.class);
            query.setParameter("username","member1");
            Member singleResult = query.getSingleResult();
            System.out.println("singleResult =" + singleResult);


            tx.commit();
        }catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();

    }

    private static void printMemberAndTeam(Member member) {
        String username = member.getUsername();
        System.out.println("username = " + username);

        Team team = member.getTeam();
        System.out.println("team=" + team);
    }
}
