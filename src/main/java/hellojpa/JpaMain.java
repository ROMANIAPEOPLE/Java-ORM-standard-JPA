package hellojpa;

import javax.persistence.*;
import java.util.List;

/** 지연로딩과 즉시로딩
 *Fetch = FetchType.LAZY 하면 , LAZY로 설정된 것은 모두 프록시 객체로 조회한다.
 * 또한 , OneToOne과 ManyToOne만 Default 값이 EAGER고, 나머지는 모두 LAZY로 설정되어있다.
 * 실제로 객체를 사용하는 시점에 프록시가 초기화되면서 쿼리가 실행된다.
 *
 * 예를들어 Member를 조회할때 Team이 ManyToOne 관계로 외래키로 잡혀있다면, Member를 조회하는 동시에
 * 자동으로 Team도 조회가 된다.
 * 이 때, Team의 FetchType을 Lazy로 설정해준다면 Member를 조회할때 Team이 조회되지 않는다.
 * 결론적으로, 실무에서 EAGER(즉시로딩)은 99프로 사용하지 말아야한다.
 *
 * **CasCade란
 * 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속상태로 만들고 싶을때 사용하는 것. 지연로딩,즉시로딩과는 아무런 관련이 없음

 *값 타입
 * -int, Integer , String 처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
 * -식별자가 없고 값만 있으므로 변경시 추적 불가
 * -예)숫자 100을 200으로 변경하면 완전히 다른 값으로 대체
 * - 값 타입은 공유하면 안된다. 예를들어 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안되기 때문에.
 * -생명주기를 엔티티에 의존한다. (회원을 삭제하면 회원 나이, 이름도 삭제된다)
 *
 * 임베디드 타입은 Setter를 만들지 말자. 생성자로만 값을 설정할 수 있도록 구현하자.

 */

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
