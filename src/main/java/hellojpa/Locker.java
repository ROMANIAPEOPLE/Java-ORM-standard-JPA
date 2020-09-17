package hellojpa;

import javax.persistence.*;

@Entity
public class Locker {

    @Id @GeneratedValue
    @Column(name ="LOCKER_ID")
    private Long id;

    @OneToOne(mappedBy = "locker")
    private Member member;

    private String name;

}
