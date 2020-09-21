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

            Team team =new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("hello");
            member.setTeam(team);
            em.persist(member);
            em.flush();
            em.clear();

            Member m = em.find(Member.class, member.getId());

            System.out.println("m = " + m.getTeam().getClass());

            System.out.println("=======================");
            m.getTeam().getName(); //초기화, LAZY면 여기서 Team 조회
            System.out.println("=======================");


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
