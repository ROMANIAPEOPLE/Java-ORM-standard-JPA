package hellojpa;


import jdk.vm.ci.meta.Local;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
public class Member extends BaseEntity {
    @Id //PK(기본키 맵핑)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "MEMBER_ID")
    private Long id;

    @Column(name ="USERNAME")
    private String username;



    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="TEAM_ID")
    private Team team;

    @OneToOne
    @JoinColumn(name ="LOCKER_ID")
    private Locker locker;

    //기간 Period

    @Embedded
    private Period period;

    //주소 Address
    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name ="FAVORITE_FOOD", joinColumns =
    @JoinColumn(name= "MEMBER_ID"))
    @Column(name = "FOOD_NAME")
    private Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "ADDRESS" , joinColumns =
    @JoinColumn(name = "MEMBER_ID"))
    private List<Address> addressHistory = new ArrayList<>();




    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }





}
