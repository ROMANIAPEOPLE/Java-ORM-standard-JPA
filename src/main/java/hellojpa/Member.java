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
    @Column(name= "MEMBER_ID")
    private Long id;

    @Column(name ="USERNAME")
    private String username;

    @ManyToOne
    @JoinColumn(name="TEAM_ID")
    private Team team;

    @Column(name = "TEAM_ID")
    private Long teamId;


}
