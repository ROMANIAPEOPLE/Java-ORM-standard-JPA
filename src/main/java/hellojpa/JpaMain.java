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

            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");

            //연관관계 편의메소드
            member.changeTeam(team);


            Team findTeam = em.find(Team.class, team.getId()); //1차 캐시에 있음
            List<Member> members = findTeam.getMembers();
            //아직 DB에는 쿼리문이 쏴지기 전이고, 1차캐시에만 있는 상태임. 따라서 연관관계 편의메서드를
            //사용하지 않으면, team에 어떤 member가 속해있는지 추출할 수 없음.



            tx.commit();
        }catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();

    }
}
