package hellojpa;


import javax.persistence.*;

//고급매핑: 상속
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public class Item {

    @Id @GeneratedValue
    private Long id;
    private String name;
    private int price;
}
