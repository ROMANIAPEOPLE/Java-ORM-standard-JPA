package hellojpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
public class Member {
    @Id
    private Long id;
    private String name;


    public Member(){

    }

    public Member(Long id , String name) {
        this.id = id;
        this.name=name;
    }
}
