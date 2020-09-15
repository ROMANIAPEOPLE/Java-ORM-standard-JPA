package hellojpa;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class Member {
    @Id //PK(기본키 맵핑)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name") //객체명은 username인데 DB에는 name일때
    private String username;
    private Integer age;

    @Enumerated(EnumType.STRING) //Enum타입을 사용할때
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP) //날짜
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP) //날짜
    private Date lastModifiedDate;

    @Lob //매우 큰 컨텐츠를 넣을때
    private String description;

    public Member() {

    }

}
