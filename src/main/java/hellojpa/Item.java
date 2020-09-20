package hellojpa;


import javax.persistence.*;

//고급매핑: 상속
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn
public abstract class Item extends  BaseEntity {

    @Id @GeneratedValue
    private Long id;
    private String name;
    private int price;
}
