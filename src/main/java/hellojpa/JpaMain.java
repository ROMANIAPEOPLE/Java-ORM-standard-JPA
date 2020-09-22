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
            member.setAge(10);
            em.persist(member);

            em.flush();
            em.clear();

            List<MemberDTO> result = em.createQuery("select new hellojpa.MemberDTO(m.username, m.age) from Member m ", MemberDTO.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();

            MemberDTO memberDTO = result.get(0);
            System.out.println(memberDTO.getUsername());
            System.out.println(memberDTO.getAge());

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
