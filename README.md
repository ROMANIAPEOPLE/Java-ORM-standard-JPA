# Java-ORM-standard-JPA


<details>
  <summary>1.영속성 컨텍스트</summary>
  <div markdown="1">
  

### 1. 영속성 컨텍스트

-영속성 컨텍스트는 JPA에서 가장 중요한 개념중 하나로,  **논리적인 개념, 눈에 보이지 않으며 Entity를 영구히 저장하는 환경** 이라는 뜻이다.



영속성 컨텍스트를 이해하기전에 먼저 EntityManagerFactory와 EntityManager를 간단하게 이해하고 넘어가자.



- EntityManagerFactory는 고객이 요청이 올 때마다 (쓰레드가 하나 생성될 때마다) EntityManager를 생성한다.
- EntityManager는 내부적으로 DB connection pool을 사용해서 DB에 접근한다.
- EntityManagerFactory
  - JPA 는 EntityManagerFactory 를 만들어야 한다.
  - application loading 시점에 DB 당 딱 하나만 생성되어야 한다.
  -  WAS 가 종료되는 시점에 EntityManagerFactory 를 닫는다. 그래야 내부적으로 Connection pooling 에 대한 Resource 가 Release 된다.

- EntityManager
  - 실제 Transaction 단위를 수행할 때마다 생성한다.
  - 즉, 고객의 요청이 올 때마다 사용했다가 닫는다.
  - thread 간에 공유하면 안된다. (사용하고 버려야 한다.)
- EntityTransaction
  - Data 를 “변경”하는 모든 작업은 반드시 Transaction 안에서 이루어져야 한다.
  - 단순한 조회의 경우는 상관없음.
  - 

엔티티의 생명주기는 크게 4가지로 나뉜다.

1. 비영속(new/ transient ) 상태 : 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태

   ![비영속상태](https://user-images.githubusercontent.com/39195377/97031972-06513980-159c-11eb-81c4-fde00dc09533.PNG)

   -객체를 단순히 '생성만' 한 상태로, 영속성 컨텍스트와는 전혀 관계가 없다.

   ```java
   Member member = new Member();
   member.setId("member1");
   member.setUsername("회원1");
   ```

   

2. 영속(managed) 상태 : 영속성 컨텍스트에 **관리** 되는 상태

  ![영속상태](https://user-images.githubusercontent.com/39195377/97031995-0cdfb100-159c-11eb-8bf9-e03d3a4b43eb.PNG)


   -영속성 컨텍스트에 저장되어있는 상태

   -persist는 DB에 쿼리를 날려 DB에 저장하는 작업이 아닌, 객체(Entity)를 영속성 컨텍스트에 저장하는 작업

   ```java
   // 객체를 생성한 상태 (비영속)
   Member member = new Member();
   member.setId("member1");
   member.setUsername("회원1");
   EntityManager entityManager = entityManagerFactory.createEntityManager();
   entityManager.getTransaction().begin();
   // 객체를 저장한 상태 (영속)
   entityManager.persist(member);
   ```

   

3. 준영속(detached) 상태 : 영속성 컨텍스트에 있다가 빠져나온(분리)된 상태

   -영속성 컨텍스트에서 지운 상태

   ```java
   // 회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
   entityManager.detach(member);
   ```

   

4. 삭제(removed) 상태 : 삭제된 상태

   -실제로 DB에서 삭제를 요청한 상태

   ```java
   // 객체를 삭제한 상태
   entityManager.remove(member);
   ```



★1차캐시 : 영속성 컨텍스트에는 1차캐시라는것이 존재하는데, 1차캐시를 영속성 컨텍스트라고 이해해도 좋다.

![1차캐시](https://user-images.githubusercontent.com/39195377/97031946-00f3ef00-159c-11eb-9a44-cdbbc0414d51.PNG)

```java
//엔티티를 생성한 상태(비영속) 
Member member = new Member(); 
member.setId("member1"); 
member.setUsername("회원1");   
//엔티티를 영속(1차캐시에 저장)
em.persist(member);
```



**영속성 컨텍스트는 아래와 같은 메커니즘을 가지고 동작한다. Member라는 객체로 예를 들겠다.**

1. 먼저 JPA에서 member라는 객체를 조회하면, DB에 select 쿼리문을 날려서 조회를 하기 전에 1차캐시에 조회하려던 member가 저장되어있는지 확인한다.
2. 만약 1차캐시에 **이미 저장되어있다면** 조회 쿼리를 날리지 않고 1차 캐시에 있는 데이터를 가져온다.
3. 만약 1차 캐시에 데이터가 존재하지 않는다면 조회 쿼리를 날린 후 DB에서 조회를 하고 1차캐시에 저장한 다음에 값을 반환한다.

그러나 사실 1차 캐시는 큰 성능 이점을 가지고 있지는 않다. 

EntityManger는 트랜잭션 단위로 만들고, 해당 DB 트랜잭션이 종료될때 같이 종료된다. 즉 1차 캐시도 모두 소멸되기 때문에 아주 짧은 찰나의 순간에만 성능 이점을 가진다.



★영속성 컨텍스트는 동일성을 보장한다.

```java
Member a = entityManager.find(Member.class, "member1");
Member b = entityManager.find(Member.class, "member1");
System.out.println(a == b); // 동일성 비교 true
```

- 영속 Entity 동일성(==비교) 를 보장해준다.
- member1 이라는 Entity를 2번 조회하면, select 조회 쿼리가 한번만 나가고, 그 이후에 조회되는것은 1차캐시에 가져오기때문에 조회쿼리가 나가지 않는다.



★엔티티 등록시, 트랜잭션을 지원하는 쓰기지연

```java
transaction.begin(); // Transaction 시작
entityManager.persist(memberA);
entityManager.persist(memberB);
// 이때까지 INSERT SQL을 DB에 보내지 않는다.
// 커밋하는 순간 DB에 INSERT SQL을 보낸다.
transaction.commit(); // Transaction 커밋 

```

![쓰기지연](https://user-images.githubusercontent.com/39195377/97031981-09e4c080-159c-11eb-95e1-bdf569314fb8.PNG)

쓰기지연은 아래와 같은 메커니즘으로 실행된다.

1. entityManager.persist(memberA) 가 실행되면, memberA를 1차캐시에 저장한다.
2. 1)과 동시에 JPA가 Entity를 분석하여 insert 쿼리를 만든다.
3. insert 쿼리를 바로 실행하지 않고 쓰기 지연 SQL 저장소에 쌓아둔다.
4. memberB도 동일하게 이루어진다.
5. tansaction.commit() 호출과 동시에 쓰기지연 SQL 저장소에 쌓여있는 쿼리들을 실행한다.



★JPA는 엔티티 '수정'시 변경 감지(Dirty Checking)을 지원한다.

```java
transaction.begin(); // Transaction 시작

// 영속 엔티티 조회
Member memberA = em.find(Member.class, "memberA");

// 영속 엔티티 데이터 수정
memberA.setUsername("hi");
memberA.setAge(10);

transaction.commit(); // Transaction 커밋
```

위 코드는 이미 생성된 entity의 정보를 변경하는 과정이다. 이 과정을 보면 아래와 같은 의문이 생길 수 있다.

```java
 em.update(member) 또는 em.persist(member) 
     //이런 코드가 있어야 하지 않을까???
```

아니다. 필요없다. Entity 데이터만 수정하고 commit하면 알아서 DB에 반영된다.

즉, 데이터를 set하면 해당 데이터의 변경을 감지하여 자동으로 UPDATE 쿼리가 실행된다.

**(실무에서는 set 사용을 지양해야 한다.)**

![변경감지](https://user-images.githubusercontent.com/39195377/97031963-04877600-159c-11eb-830d-96fa1dbff703.PNG)

변경 감지는 아래와같은 매커니즘으로 동작한다.

1. commit()을 하면 , <u>**flush()**</u> 가 일어날때 1차캐시에 있는 스냅샷과 일일이 비교한다.
2. 변경사항이 있으면 UPDATE 쿼리를 만들어서 쓰기 지연 SQL에 쌓아둔다.
3. UPDATE 쿼리 실행 후 commit() 한다

<u>**flush()**</u> : 영속성 컨텍스트에 저장된 데이터들을 DB에 반영하는 작업 (commit할때 자동으로 발생) 이다. <u>여기서 주의해야 할 점은, flush()는 영속성 컨텍스트의 값을 비우는 작업이 아니고, 단순히 DB에 반영하는 작업이다.</u>



JPA에서 flush는 아래 세 가지 상황에 발생한다.

1. entityManager.flush() 로 직접 호출
2. 트랜잭션 커밋 
3. JPQL 쿼리를 실행

3번의 경우(JPQL 쿼리 실행시 자동으로 flush)의 이유는 아래와 같다.

```java
entityManager.persist(memberA);
entityManager.persist(memberB);
entityManager.persist(memberC);

//바로 아래에 JPQL을 실행한다고 가정해보자. 
//여기선 간단하게 member를 조회하는 JPQL을 실행한다고 가정
query = entityManager.createQuery("select m from member m", Member.class);
List<Member> members = query.getResultList();
```

만약 위의 코드와 같이 memberA~memberC까지 영속성 컨텍스트에 저장했다고 가정하자.

아직 트랜잭션 커밋이 일어나기 전이라 memberA~memberC는 DB에 저장되기 전 상태이다.

따라서 JPQL 실행시 아무런 값도 반환되지 않는다.

이러한 문제를 방지하기 위해 JPA는 기본적으로 JPQL 실행시 자동으로 flush를 실행한다.


  </div>
</details>

<details>
  <summary>2. 객체와 DB의 기본적인 매핑 방법</summary>
  <div markdown="1">
    
   # 객체와 DB의 기본적인 매핑 방법

### 엔티티 매핑

1. ##### @Entity

   -@Entity가 붙은 클래스는 JPA가 관리하는 '엔티티' 라고 부른다.

   -JPA를 사용해서 테이블과 매핑할 클래스에는 필수적으로 @Entity를 붙여야 한다.

   - @Entity 사용시 주의사항

     - 기본 생성자 필수

     => 자바에서는 기본 생성자를 자동으로 생성해준다. 하지만 임의로 파라미터가 있는 생성자를 사용할 경우 기본 생성자를 직접 생성해줘야 한다.

     - final class, enum, interface, inner class는 @Entity로 엔티티 등록을 할 수없다.

     - DB에 저장할 필드는 final로 선언하면 안된다.

     

   - Entity 속성

     - @Entity(name = "Member") 

       => JPA에서 사용할 엔티티 이름을 지정한다. 기본값은 [클래스 이름] 이며, **가급적이면 기본값을 사용하는것이 좋다.**

     - 기본값은 그냥 @Entity만 선언하면 된다.

2. 필드와 컬럼 매핑

   ex) 1. 회원은 일반 회원과 관리자로 구분해야 한다.

    	2. 회원 가입일과 수정일이 있어야 한다.

    	3. 회원을 설명할 수 있는 필드가 있어야 한다. 이 필드는 길이 제한이 없다.

   ```java
   public enum RoleType {
     USER, ADMIN
   }
   //회원 권한 설정을 위한 enum 타입
   ```

   

   ```java
   @Entity
   @Table(name = "MBR")
   public class Member {
     @Id
     private Long id;
   
     @Column(name = "name")
     private String username;
   
     private Integer age;
   
     @Enumerated(EnumType.String)
     private RoleType roleType;
   
     @Temporal(TemporalType.TIMESTAMP)
     private Date createDate;
   
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastModifiedDate;
   
     @Lob
     private String description;
   
     @Transient
     private int temp;
   
     public Member() {
   
     }
   }
   ```

   - @Id 사용

     - 기본키 (PK) 매핑시 사용하며, 기본키에 사용한다.

   - @Column

     - @Column(name = "name") : 객체명과 DB의 컬럼명을 다르게 하고 싶은 경우, DB 컬럼명으로 설정할 이름을 name의 속성으로 적는다.

       =>예를들어, 객체의 이름은 username인데 DB에는 name으로 저장하고 싶을때

     - updatable

       컬럼을 수정했을 때 DB에 추가를 할 것인지 여부를 선택한다.

       @Column(updatable = false) 인 경우, 변경이 되어도 DB에 반영하지 않는다.

     - nullable

       @Column(nullable = false) : NOT NULL 제약조건이 된다.

     - unique

       잘 사용하지 않는다. 그 이유는 ```constraint UK_ewkrjwel239flskdfj01 unique (name) ```과 같이 유니크 네임을 랜덤으로 생성하기 때문이다.

     - length

       문자 길이 제약조건으로, String 타입에만 사용할 수 있다.

   - @Enumerated

     - Enum Type 매핑

       Enum 객체 사용시 해당 어노테이션을 사용해야 한다.

       DB에는 Enum Type이 존재하지 않으므로 (비슷한건 존재함) 반드시 붙여줘야한다.

     - EnumType에는 두가지 속성이 있는데, ORDINAL과 String이 있다.

       ORDINAL은 enum의 순서를 DB에 저장하는 것이고(기본값), String은 enum의 이름대로 DB에 저장하는 것이다.

       **실무에서는 반드시 EnumType.String을 사용하자. 그 이유는 새로운 요구사항이 추가될때 그 새로운 요구사항을 enum class의 맨 앞에 추가할 경우 순서가 변경되어 저장되기 때문이다.**

   - Tempora

     - 날짜 Type에 붙여주는 어노테이션이다.
     - **<u>java8의 도입과 동시에 LocalDate(date), 와 LocalDateTime(timestamp)가 도입되면서, 사용할 일이 없어졌다.</u>**

   - Lob

     - DB에서 varchar를 넘어서는 큰 내용을 넣고 싶은 경우 해당 annotation을 사용
     - @Lob에는 지정할 수 있는 속성이 없다.

   - @Transient

     - 특정 필드를 컬럼에 매핑하지 않음 (DB저장,조회 불가능)

     - DB에 관계없이 메모리에서만 사용하고자 하는 객체에 해당 annotation을 사용

       즉, 메모리상에서만 임시로 어떠한 값을 보관하고 싶을때 사용한다.
       
      
      
  
  
 
 ### 기본 키 매핑
 

1. 직접 할당 : @Id만 사용



2. 자동생성의 4가지 방법(@GeneratedValue)

   1. **IDENTITY**

      - @GeneratedValue(strategy = GenerationType.IDENTITY)
      - 즉, id 값을 null로 하면 DB가 알아서 AUTO_INCREMENT 해준다.
      - 기본 키 생성을 데이터베이스에 위임한다.

      ```java
      public class Member {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id; 
      }
      ```

      ```mysql
      // H2
      create table Member (
        id varchar(255) generated by default as identity,
        ...
      )
      // MySQL
      create table Member (
        id varchar(255) auto_increment,
        ...
      )
      ```

      - IDENTITY 전략은 entityManager.persist 시점에 즉시 INSERT 쿼리를 실행하고, DB에서 식별자를 조회한다.

        ->이 전략은 ID값을 설정하지 않고 INSERT 쿼리를 날리며, 그때 id값을 자동으로 생성한다.  AUTO_INCREMENT는 DB에 INSERT SQL을 실행한 이후에 id 값을 알 수 있다.
        즉, id 값은 DB에 값이 들어간 이후에서야 알 수 있다는 것이다.

      - ID값을 DB에 값이 저장된 이후에 알게 되었을때 문제점은 ?

        ->영속성 컨텍스트에서 해당 객체가 관리되려면 무조건 기본키(PK) 값이 있어야 한다.

        ->하지만 이 경우 PK값은 DB에 들어가봐야 (commit 이후) 알 수 있다.

        ->다시 말해서 IDENTITY 전략의 경우 영속성 컨텍스트의 1차 캐시 안에 있는 @Id 값은 DB에 넣기 전까지는 세팅을 할 수 없다는 것이다.

        - 이 문제를 해결하기 위해, IDENTITY 전략에서만 예외적으로 persist() 시점에 바로 DB의 INSERT 쿼리를 날린다.

   2. SEQUENCE

      - 데이터베이스의 Sequence Object를 사용한다.
      - DB Sequence는 유일한 값 순서대로 생성하는 특별한 데이터베이스 오브젝트다.

   3. TABLE

      - @GeneratedValue(strategy = GenerationType.TABLE)
      - 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
      - @TableGenerator 필요
      - 모든 DB에 적용 가능하지만, 최적화 되어있지 않은 테이블을 직접 사용하기 때문에 성능 이슈가 있다.
      - 운영서버에서 사용하기에 적합하지 않는 전략이다.

   4. AUTO

      - @GeneratedValue(strategy = GenerationType.AUTO)
      - 기본 설정 값
      - 방언에 따라 위의 세가지 전략을 자동으로 저장한다.



​	★권장하는 식별자 구성 전략

​	**(Long형) + (대체키) + (적절한 키 생성 전략)**

 	1. LongType 사용
 	2. 대체키 사용: 랜덤 값, 유휴 ID 등 비즈니스와 전혀 관계없는 값 사용
 	3. AUTO_INCREMENT 또는 Sequnce Object 사용



​	[Long Type을 사용해야 하는 이유]

- int : 0이 있다.
- Integer : 10억 정도 까지만 가능하다
- Long : 이걸 사용하자!
  </div>
</details>


<details>
  <summary>3.단방향 연관관계와 매핑의 기초</summary>
  <div markdown="1">
    
   # 단방향 연관관계와 매핑의 기초

### 목표

- 객체와 테이블 연관관계의 차이를 이해한다.
- 객체의 참조와 테이블의 외래키를 어떻게 매핑하는지 이애햐한다.
- 방향과 매핑종류, 연관관계 주인이라는 용어를 이해한다.
  - 단방향, 양방향
  - 다대일(N:1) , 일대다(1:N), 일대일(1:1) , 다대다(N:N)
  - 연관관계의 주인(Owner)
    - 객체의 양방향 연관관계에서는 관리하는 주인이 필요하다.



### 예제 시나리오

- 회원과 팀이 있다

- 회원은 하나의 팀에만 소속될 수 있다.

- 회원과 팀은 다대일(N:1) 관계이다.

  ->  여러 회원이 하나의 팀에 속할 수 있다.



1. 객체를 테이블에 맞추어 모델링하기 (연관관계가 없는 객체)
![객체를 테이블메 마추어서](https://user-images.githubusercontent.com/39195377/97140495-189bc500-17a0-11eb-9ae9-041534bedccb.PNG)

   **Member**

   ```java
   @Entity
   public class Member {
     @Id
     @GeneratedValue
     private Long id;
     
     @Column(name = "USERNAME")
     private String username;
     
     private int age;
     
     @Column(name = "TEAM_ID")
     private Long teamId;
     ...
   }
   ```

   **Team**

   ```java
   @Entity
   public class Team {
     @Id
     @GeneratedValue
     private Long id;
     
     private String name;
     ...
   }
   ```

   위의 코드처럼 객체를 테이블에 맞추어 모델링 했을 때의 문제점은 무엇인가?

   - 저장의 경우를 살펴보자

     ```java
     // 팀 저장
     Team team = new Team();
     team.setName("TeamA");
     entityManager.persist(team);	// PK값이 세팅된 상태 (영속 상태)
     // 회원 저장
     Member member = new Memeber();
     member.setName("member1");
     member.setTeamId(team.getId());	// 외래키 식별자를 직접 다룸  
     entityManager.persist(member);
     ```

     바로 **외래키의 식별자를 직접 다뤄야 하는(저장해야하는) 문제점이 있다.**

     - Team 객체를 영속화 한 후 Member에 팀을 설정할때, 외래키인 TeamId도 함께 직접 세팅해줘야 한다.
     - 물론 애플리케이션의 구동은 정상적으로 이루어지지만 객체 지향적인 방법이 아니다.

   - 다음으로 조회의 경우를 살펴보자.

     ```java
     // 조회
     Member findMember = entityManager.find(Member.class, member.getId());
     // 연관 관계가 없음 (못가져옴)
     Team findTeam = entityManger.find(Team.class, team.getId());
     ```

     ```java
     // 먼저 식별자를 가져와야 함 (가져옴)
     Long findTeamId = findMember = findMember.getTeamId();
     Team findTeam = entityManger.find(Team.class, team.getId());
     ```

     - 연관관계가 없기 때문에 식별자를 통해 다시 팀을 조회해야 한다.
     - 즉, 조회를 두 번 해야 하며, 객체 지향적인 방법이 아니다.

 ★결론적으로, 객체를 테이블에 맞추어 데이터 중심으로 모델링 하면 협력 관계를 만들 수 없다.

- 테이블 : 외래키로 조인을 사용해서 연관된 테이블을 찾는다.
- 객체 : 참조를 사용해서 연관된 객체를 찾는다.

​     => 테이블과 객체 사이에는 이러한 큰 간격이 존재하게 되는 것이다.





2. 객체 지향 모델링(객체의 연관관계 사용)

   ![다대일연관관계](https://user-images.githubusercontent.com/39195377/97140497-189bc500-17a0-11eb-95d4-470dd0b49d95.PNG)

   ```java
   @Entity
   public class Member {
     @Id
     @GeneratedValue
     @Column(name = "MEMBER_ID")
     private Long id;
     
     @Column(name = "USERNAME")
     private String username;
     
     private int age;
     
   //기존 연관관계 삭제
   //  @Column(name = "TEAM_ID")
   //  private Long teamId;
     
     @ManyToOne
     @JoinColumn(name = "TEAM_ID") // 매핑 
     private Team team;
     ...
   }
   ```

   - 외래 키 대신에 Team 객체를 넣고 TEAM_ID를 매핑한다.

   - 조인할 컬럼을 명시한다

     - JoinColumn으로 조인 컬럼을 명시한다.
     - 적지 않으면 Default값이 들어가지만, 적어주도록 하자.

   - 연관관계를 표시한다.

     - Member의 입장에서 Team은 다대일(N:1) 이다 : 여러명의 멤버가 하나의 팀에 가입할 수 있다.
     - 반대로 Team의 입장에서는 일대다(1:N) 이다. 
     - @ManyToOne 으로 연관관계를 설정하고, Team이라는 필드가 DB의 "TEAM_ID" 라는 외래 키와 매핑된다.

     ![ORM연관관계](https://user-images.githubusercontent.com/39195377/97140494-176a9800-17a0-11eb-973f-cddab167f8fc.PNG)




​		객체 지향 모델링 기준으로 저장과 조회를 살펴보자.	

```java
// 팀 저장
Team team = new Team();
team.setName("TeamA");
entityManager.persist(team);
// 회원 저장
Member member = new Memeber();
member.setName("member1");
member.setTeam(team);    // 단방향 연관관계 설정, 참조 저장
entityManager.persist(member);

Member findMember = entityManager.find(Member.class,  member.getId());
```

저장의 경우, 테이블에 맞추어 연관관계를 매핑하던 것과 다르게 setTeam에 참조를 저장한다.



조회의 경우도, 조회쿼리를 한번만 쓰면 된다.

```java
// 조회
Member findMember = em.find(Member.class, member.getId());
// 참조를 사용해서 연관관계 조회
Team findTeam = findMember.getTeam();
```


3. 객체 지향 모델링(객체의 연관관계 사용) - 양방향 연결관계

  ![양방향](https://user-images.githubusercontent.com/39195377/97143841-c5794080-17a6-11eb-95ca-0486d4b1b890.PNG)

   ```java
   @Entity
   public class Member { 
   
       @Id @GeneratedValue    
       private Long id;
   
       @Column(name = "USERNAME")    
       private String name;    
       private int age;
   
       @ManyToOne    
       @JoinColumn(name = "TEAM_ID")    
       private Team team;
       
      ...
   ```

   Team 엔티티에 컬렉션을 추가하면 양방향 매핑 완료

   ```java
   @Entity
   public class Team{
       @Id @GeneratedValue
       private Long id;
       private Sting name;
       @OneToMany(mappedBy = "team")
       List<Member> members =new ArrayList<Member>();
   }
   ```

   ex)

   ```java
   Team findTeam = entityManaer.find(Team.class, team.getId);
   int memberSize = findTeam.getMembers.size();
   ```

- 아래 그림처럼 객체 기준으로 보았을때는  Member -> Team , Team -> Member 로 단방향이 2개가 있다고 생각하면 된다.

- 그러나 테이블 연관관계는  Member <=> Team 으로, 하나의 양방향 관계를 이룬다.

- 즉, 객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단방향 관계 2개이다.

 ![객체는단방향테이블은양방향](https://user-images.githubusercontent.com/39195377/97143808-b72b2480-17a6-11eb-9d0a-a9195d100bfd.PNG)



#### 테이블은 양방향 연관관계에서 '외래키'라는 것으로 관리해야한다.

- 테이블은 외래키 하나로 두 테이블의 연관관계를 관리한다.
- 위 예제에서는 두 테이블을 관리하는 외래키로 'TEAM_ID'를 사용하고 있다.
- **연관관계의 주인을 정하자.**



#### 연관관계의 주인을 정하자.

- 객체의 두 관계중 하나를 연관관계의 주인으로 지정
- 연관관계의 주인만이 외래키를 관리한다.
- 주인이 아닌쪽은 읽기만 가능(읽기 전용) 하다.
- 주인인 쪽인 mappedBy 속성을 사용하면 안된다.
- 주인이 아닌쪽에서 mappedBy 속성으로 주인을 지정해줘야 한다.
- **<u>그렇다면, 누구를 주인으로 설정해야 할까?</u>**

#### 연관관계의 주인은 **항상** , **무조건**  '다' 쪽으로 설정하자!

![멤버가연고나관계주인](https://user-images.githubusercontent.com/39195377/97143814-b8f4e800-17a6-11eb-87d5-373e20cb3d70.PNG)




★양방향 매핑시 가장 많이하는 실수 : 연관관계의 주인에 값을 입력하지 않는다.

```java
Team team = new Team();
team.setName("TeamA");
entityManager.persist(team);

Member member = new Member();
member.setName("member1");

//역방향(주인이 아닌 쪽)만 연관관계를 설정
team.getMembers().add(member);

entityManager.persist(member);

```



- mappedBy는 단순히 읽기 전용이다.
- 만약 Member를 생성하고 Team을 생성한 다음 Team에있는 mappedBy와 매핑된 컬렉션에 member의 값을 세팅해도, 아래 결과처럼 Member DB에는 아무런 변화가 없다.

![결과1](https://user-images.githubusercontent.com/39195377/97143813-b85c5180-17a6-11eb-89a3-8c8077791eb7.PNG)



양방향 매핑시 연관관계의 주인에 값을 입력해야한다.

```java
Team team = new Team();
team.setName("TeamA");
entityManager.persist(team);

Member member = new Member();
member.setName("member1");

team.getMembers().add(memeber);
//연관관계의 주인에 값 설정
member.setTeam(team);

entityManager.persist(member);
```

이렇에 연관관계의 주인에 값을 입력하면 정상적으로 DB에 저장된다.

![겨로가2](https://user-images.githubusercontent.com/39195377/97143811-b85c5180-17a6-11eb-9beb-e968124b908e.PNG)


**하지만 순수한 객체 관계를 고려한 객체지향적 설계를 위해 양쪽 모두 값을 넣어주는 습관들 들이자.**

- 주인이 아닌쪽과 주인인 쪽 모두 값을 넣어주는 연관관계 매핑 메서드를 만들자.

```java
 public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }
```







#### 정리

★단방향 매핑와 양방향 매핑은 어렵게 생각할 필요 없이,

-  일단 먼저 단방향 매핑으로 설계를 하자.
- 그 다음에, 양방향이 필요할때 추가하도록 하자
- 이렇게 설계해도 테이블에는 전혀 영향을 주지 않는다. 





  </div>
</details>


<details>
  <summary>4. 연관관계 매핑의 4가지 종류</summary>
  <div markdown="1">
   
   # 연관관계 매핑의 4가지 종류

 들어가기 전에 

- 연관관계는 사실상 방향이라는 개념이 존재하지 않는다. 외래키 하나로 양쪽을 조인 가능하다.
- 연관관계의 주인은 항상 '다' 즉 [N] 쪽에 설정해줘야 한다.
- 참조용 필드(mappedBy)는 읽기 전용으로, 오로지 참조만 가능하다.
- 객체에서의 양방향은 A->B, B->A 처럼 참조가 2군데인 것이다.



1. 다대일 [N:1]
2. 일대다 [1:N]
3. 일대일 [1:1]
4. 다대다 [N:N]







### 1. 다대일[N:1] 

- JPA에서 가장 많이 사용하고, 꼭 알아야 하는 다중성이다.
- 아래 테이블에서 보면 DB설계상 일대다에서 '다' 쪽에 외래키가 존재해야한다. 그렇지 않으면 잘못된 설계이다.
- 테이블에서는 FK가 팀을 찾기 위해 존해하고, 객체에서 Team 필드도 Team을 참조하기 위해 존재한다.

 1-1. 다대일 단방향 매핑

- JPA의 @ManyToOne 어노테이션을 사용해서 다대일 관계를 매핑한다.
- @JoinColumn은 외래키를 매핑할 때 사용한다. name은 매핑할  외래 키의 이름이다.

```java
public class Member{
    ...
    @ManyToOne
    @JoinCoulmn(name="TEAM_ID")
    private Team team;
}
```

![다대일단방향](https://user-images.githubusercontent.com/39195377/97150589-b21fa280-17b1-11eb-9128-a9c2abad86eb.PNG)



1-2. 다대일 양방향 매핑

- 다대일 관계에서 단방향 매핑을 진행하고, 양방향 매핑을 진행할때 사용한다.
- 반대쪽에서 일대다 단방향 매핑을 해주면 된다.(객체기준으로, 컬렉션을 추가하자)
- 여기서 중요한건, 반대에서 단방향 매핑을 한다고 해서 DB테이블에 영향을 전혀 주지 않는다.
- 다대일의 관계에서 **다 쪽에서 이미 연관관계 주인**이 되어서 외래키를 관리하고 있다.



![다대일양방향](https://user-images.githubusercontent.com/39195377/97150625-bcda3780-17b1-11eb-81ca-ef9fe1561def.PNG)

- 반대쪽에서 일대다 단방향 매핑. JPA의 @OneToMany 어노테이션을 사용한다.
- 연관관계의 주인이 아니고, 어디에 매핑 되었는지에 대한 정보를 표시하는 (mappedBy="team") 을 꼭 넣어줘야 한다.
- (mappedBy = "team") 에서 team은  Member에서 외래키로 매핑된 필드명이다.

```java
public class Team {
    ...

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
    
    ...
}
```



정리

- 외래키가 있는 쪽이 연관관계 주인이다.
- 양쪽을 서로 참조하도록 개발하자.





### 2. 일대다[1:N]

- 일대다 관계에서는 일이 연관관계의 주인이다.
- 일 쪽에서 외래키를 관리하겠다는 의미가 된다
- 결론 먼저 말하자면, 표준스펙에서 지원은 하지만 **실무에서는 사용을 권장하지 않는다.**



  2-1. 일대다 단방향 매핑

![일대다단방향](https://user-images.githubusercontent.com/39195377/97150684-d54a5200-17b1-11eb-9c6c-47bb72e2085f.PNG)


- 팀과 멤버가 일대다 관계이다.
- Team이 Members(컬렉션)을 가지는데, Member의 입장에서는 Team을 참조하지 않아도 된다는 설계이다. '객체'의 입장에서 생각해보면 충분히 나올수 있는 설계이다.
- 그러나 DB 테이블 입장에서 보면 무조건 일대다에서 '다' 쪽에 외래키가 들어간다.
- Team에서 members가 바뀌면, DB의 member 테이블에서 업데이트 쿼리가 나가는 상황이 발생한다.



 JPA의 @OneToMany와 @JoinColumn()을 이용해 일대다 단방향 매핑을 설정할 수 있다.

- Member는 코드상 연관관계 매핑이 없고, 팀에서만 일대다 단방향 매핑 설정

  ```java
  @Entity
  public class Team {
      ...
      @OneToMany
      @JoinColumn(name = "TEAM_ID")
      private List<Member> members = new ArrayList<>();
  ```

  

```java
Member member = new Member();
member.setUsername("MemberA");
em.persist(member);

System.out.println("-----멤버 저장");

Team team = new Team();
team.setName("TeamA");
team.getMembers().add(member);
em.persist(team);

System.out.println("-----팀 저장");

tx.commit();
```

- 이렇게 실행하면 쿼리에서 트랜잭션 커밋 시점에 create one-to-many row로 시작되는 주석과 함께 Member 테이블을 업데이트 하는 **쿼리가 나간다.**



★일대다 단방향의 정리

- 일대다 단방향은 일대다의 일이 연관관계 주인이다.
- 테이블 일대다 관계는 항상 다(N)쪽에 외래키가 있다.
- 객체와 테이블의 **패러다임 차이** 때문에 객체의 반대편 테이블의 외래키를 관리하는 특이한 구조다.
- @JoinColumn을 반드시 사용해야 한다. 그렇지 않으면 조인 테이블 방식을 사용한다.



★일대다 단방향 매핑의 문제점과 해결방안

- 일단 업데이트 쿼리가 나간다. 성능상 좋지 않으나 크게 문제되지는 않는다.
- 위 예시로 살펴보면, Team의 엔티티를 수정했는데 Member의 엔티티가 업데이트되는 상황이 발생한다.
- 테이블이 적을때는 크게 문제가 되지 않지만 실무에서는 테이블이 수십개가 엮어서 돌아간다.

- 참고로, 일대다 양방향 매핑은 JPA 공식적으로 존재하지 않는 방법이다.

### 따라서, 다대일 단방향 매핑을 사용하고, 필요시 양방향 설정을 사용하자.





### 3. 일대일[1:1]

- 일대일 관계는 그 반대도 일대일이다.
- 일대일 관계는 특이하게 주 테이블이나 대상 테이블 중에 외래 키를 넣을 테이블을 선택 가능하다.
  - 주 테이블에 외래키 저장
  - 대상 테이블에 외래 키 저장
- 외래키에 데이터베이스 유니크 제약조건이 추가되어야 일대일 관계가 가능하다.



3-1. 일대일 - 주 테이블에 외래키 단방향
![일대일 주테이블 단방향](https://user-images.githubusercontent.com/39195377/97150731-e98e4f00-17b1-11eb-97ed-a73edfec651a.PNG)


- 회원이 딱 하나의 락커를 가지고 있는 상황이다. 반대로 락커도 회원 한명만 할당 받을 수 있다. 이때, 둘의 관계는 일대일 관계이다.
- 이 경우 멤버를 주 테이블로 보고 주 테이블 또는 대상 테이블에 외래키를 지정할 수 있다.
- 다대일[N:1] 단방향 관계와 JPA 어노테이션만 달라지고 거의 유사하다.



3-1-2. 일대일- 주 테이블에 외래키 양방향

- 다대일[N:1] 양방향 매핑처럼 외래키가 있는곳이 연관관계의 주인이다.

- JPA @OneToOne 어노테이션으로 일대일 단방향 관계를 매핑하고, @JoinColumn을 넣어준다.

  - 여기까지만 매핑하면 단방향 관계이고, 

    ```java
    @Entity
    public class Member {
        ...
            
        @OneToOne
        @JoinColumn(name = "locker_id")
        private Locker locker;
     
        ...
    }
    ```

- 반대편에 mappedBy를 적용시켜주면 일대일 양방향 관계 매핑이 된다.

  ```java
  @Entity
  public class Locker {
      ...
          
      @OneToOne(mappedBy = "locker")
      private Member member;
  }
  ```

- 마찬가지로, mappedBy는 읽기 전용으로 참조만 가능하다.



3-2-1. 일대일 - 대상 테이블에 외래키 단방향

- 일대일 관계에서 대상 테이블에 외래키를 단방향으로 저장하는 방식은 지원하지 않는다.



3-2-2. 일대일 - 대상 테이블에 외래키 양방향

- 일대일 주 테이블에 외래 키 양방향 매핑을 반대로 뒤집었다고 생각하면 된다. 매핑 방법은 같다.

- 주 테이블은 멤버 테이블이지만, 외래 키를 대상 테이블에서 관리하고 주 테이블의 락커 필드는 읽기 전용이 된다.

  

일대일 정리

- 주 테이블에 외래키

  - (주 테이블은 많이 접근하는 테이블로 설정하자.)
  - 주 테이블에 외래키를 두고 대상 테이블을 찾는 방식
  - 객체지향 개발자들이 선호하고, JPA 매핑이 편리하다
  - 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능하다.
  - 그러나, 값이 없으면(예를들어, member가 locker를 사용하지 않음) NULL을 허용해야한다.

- 대상 테이블에 외래키

  - 대상 테이블에 외래 키가 존재한다.

  - 전통적인 데이터베이스 개발자들이 선호하는 방식이다. NULL을 허용해야하는 문제도 없다.

  - 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조를 유지할 수 있

  - 코드상에서는 주로 멤버 엔티티에서 락커를 많이 엑세스 하는데, 어쩔 수 없이 양방향 매핑을 해야한다.

    - 일대일 - 대상 테이블에 외래 키 단방향 매핑을 JPA에서 지원하지 않으므로, 단방향 매핑만 해서는 멤버 객체를 업데이트 했을 때 락커 테이블에 FK를 업데이트 할 방법이 없다. 따라서 양방향 매핑을 해야 한다. 

    

    

    ### 4. 다대다[N:N]

    - 결론부터 말하자면, 다대다는 실무에서 사용하지 말아야 한다.
    - 다대다 (@ManyToMany)는 각각 일대다, 다대일 관계로 풀어서 사용하자.
    - 연결 테이블용 엔티티를 추가한다. 연결 테이블을 엔티티로 승격시킨다.
    - JPA가 만들어준 숨겨진 매핑테이블의 존재를 바깥으로 꺼낸다고 생각하자.

    

  ![다대다](https://user-images.githubusercontent.com/39195377/97150771-f4e17a80-17b1-11eb-8256-9962af9d1559.PNG)


    

    위 그림과 같은 다대다 관계를 아래처럼 풀어낼 수 있다.

    

    - Member 엔티티에서 @OneToMany 관계로 변경한다.

      ```java
      @Entity
      public class Member {
          ...
              
          @OneToMany(mappedBy = "member")
          private List<MemberProduct> memberProducts = new ArrayList<>();
      ​
          ...
      }
      ```

      

    - Product도 마찬가지로 @OneToMany 관계로 변경한다.

      ```java
      @Entity
      public class Product {
      
          ...
      
          @OneToMany(mappedBy = "product")
          private List<MemberProduct> members = new ArrayList<>();
          
          ...
      }
      ```

    

    - MemberProduct
      - 연결 테이블을 엔티티르 승격시킨 테이블이다. 그리고 @ManyToOne 매핑을 두개 한다.
      - 여기서 추가 데이터가 들어간다면 아예 의미있는 엔티티 이름으로 변경 될 것이다.

    ```java
    @Entity
    @Getter
    @Setter
    public class MemberProduct {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        @ManyToOne
        @JoinColumn(name = "member_id")
        private Member member;
    
        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;
    }
    ```

    

    

    
  </div>
</details>

<details>
  <summary>5. 고급 매핑(상속매핑/MappedSuperClass</summary>
  <div markdown="1">
  
  # 고급 매핑



###  상속 관계 매핑

- 객체에서는 상속이라는 개념이 존재하지만, 관계형 데이터베이스는 상속 관계가 존재하지 않는다.

- 슈퍼타입, 서브타입 관계라는 모델링 기법이 상속과 유사하다.

- 상속관계 매핑이라는 것은 객체의 상속 구조와 DB의 슈퍼타입, 서브타입 관계를 매핑하는 것이다.

  ![상속매핑1](https://user-images.githubusercontent.com/39195377/97332065-5fcba800-18bd-11eb-9d46-d22903c57922.PNG)



##### 객체의 상속 관계 예제

- Item

```java
@Entity
@Inheritance(strategy = InheritanceType.XXX) // 상속 구현 전략 선택
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
}
```

- Album

  ```java
  @Entity
  public class Album extends Item {
      private String artist;
  }
  ```

- Movie

  ```java
  @Entity
  public class Movie extends Item {
  
      private String director;
      private String actor;
  }
  ```

- Book

  ```java 
  @Entity
  public class Book extends Item {
  
      private String author;
      private String isbn;
  }
  ```

  

 #### 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법에는 3가지가 있다.

1. **각각 테이블로 변환 -> 조인전략(JOINED)**

   
![조인전략](https://user-images.githubusercontent.com/39195377/97332067-60643e80-18bd-11eb-95ab-d126d8bad349.PNG)

   - 가장 정규화 된 방법으로 구현하는 방식이다.
   - name,price가 ITEM 테이블에만 저장되고, Album, movie, book이 각자의 데이터에 저장된다.
   - @Inheritance(strategy = InheritanceType.JOINED) 전략
     - 하이버네이트 조인전략에서는 @DiscriminatorColumn을 선언하지 않으면 DTYPE 컬럼이 생성되지 않는다.
     - 어차피 조인하면 앨범인지 무비인지 알 수 있다. 그래도 DTYPE( @DiscriminatorColumn)을 선언하는 것이 명확하다. 넣어주는 습관을 들이자

   ```java
   @Entity
   @Inheritance(strategy = InheritanceType.JOINED)
   @DiscriminatorColumn // 하위 테이블의 구분 컬럼 생성(default = DTYPE)
   public class Item {
   
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
   
       private String name;
       private int price;
   }
   ```

   

   - 실제 실행된 DDL

     - 테이블 4개 생성(item,album,movie,book)
     - 하위 테이블에 외래키 제약조건 생성. 하위 테이블 입장에선 ITEM_ID가 PK 이면서 FK

     ```sql
     Hibernate: 
         create table Album (
            artist varchar(255),
             id bigint not null,
             primary key (id)
         )
     Hibernate: 
         create table Book (
            author varchar(255),
             isbn varchar(255),
             id bigint not null,
             primary key (id)
         )
     Hibernate: 
         create table Item (
            DTYPE varchar(31) not null,
             id bigint generated by default as identity,
             name varchar(255),
             price integer not null,
             primary key (id)
         )
     Hibernate: 
         create table Movie (
            actor varchar(255),
             director varchar(255),
             id bigint not null,
             primary key (id)
         )
         
         
     Hibernate: 
         alter table Album 
            add constraint FKcve1ph6vw9ihye8rbk26h5jm9 
            foreign key (id) 
            references Item
     Hibernate: 
         alter table Book 
            add constraint FKbwwc3a7ch631uyv1b5o9tvysi 
            foreign key (id) 
            references Item
     Hibernate: 
         alter table Movie 
            add constraint FK5sq6d5agrc34ithpdfs0umo9g 
            foreign key (id) 
            references Item
     ```

   - 만약 Movie의 객체를 저장하면 다음과 같은 과정이 일어난다.

     1. Insert 쿼리가 두개 나간다. Item테이블과 Movie테이블 

     2. DTYPE은 클래스 이름이 Default로 저장된다.

        ```sql
        Hibernate: 
            select
                movie0_.id as id2_2_0_,
                movie0_1_.name as name3_2_0_,
                movie0_1_.price as price4_2_0_,
                movie0_.actor as actor1_3_0_,
                movie0_.director as director2_3_0_ 
            from
                Movie movie0_ 
            inner join
                Item movie0_1_ 
                    on movie0_.id=movie0_1_.id 
            where
                movie0_.id=?
        ```

        

   

   #### 조인 전략의 장점과 단점

   - 장점
     - 테이블 정규화
     - 외래키 참조 무결성 제약조건 활용
     - 저장공간 효율화
   - 단점
     - 조회시 조인을 많이 사용, 성능 저하 우려
     - 조회 쿼리가 복잡함
     - 데이터 저장시 INSERT SQL을 2번 호출

   

   

   2. **단일 테이블 전략**

  ![단일테이블전략](https://user-images.githubusercontent.com/39195377/97332057-5f331180-18bd-11eb-9d7c-c2f76bea504a.PNG)

   - 서비스 규모가 크지 않고, 굳이 조인 전략을 선택해서 복잡한 쿼리를 사용할 필요가 없다고 판단될 때 사용한다.
   - 한 테이블에 전부다 저장하고, DTYPE으로 구분하는 방법이다.
   - INSERT 쿼리도 한 번, SELECT 쿼리도 한 번 이다.
   - **@Inheritance(strategy = InheritanceType.SINGLE_TABLE)**
     - 단일 테이블 전략에서는 @DiscriminatorColumn이 없으면 테이블 구분이 불가능하다.
     - 따라서 필수로 사용해줘야하는데, @DiscriminatorColumn을 선언하지 않아도 Default로 DTYPE이 생성된다.

   ```java
   @Entity
   @DiscriminatorColumn
   @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
   public class Item {
   
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
   
       private String name;
       private int price;
   }
   ```

   - 실제 실행된 DDL :통합 테이블이 하나 생성된다.

     ```sql
     Hibernate: 
         create table Item (
            DTYPE varchar(31) not null,
             id bigint generated by default as identity,
             name varchar(255),
             price integer not null,
             artist varchar(255),
             author varchar(255),
             isbn varchar(255),
             actor varchar(255),
             director varchar(255),
             primary key (id)
         )
     ```

   - 만약 저장, 조회를 실행하면 ?

     - 조인 전략과 다르게 조인하지 않는다. 그냥 Item 테이블을 조회하고, DTYPE을 검색조건으로 추가한다.

     ```sql
     Hibernate: 
         select
             movie0_.id as id2_0_0_,
             movie0_.name as name3_0_0_,
             movie0_.price as price4_0_0_,
             movie0_.actor as actor8_0_0_,
             movie0_.director as director9_0_0_ 
         from
             Item movie0_ 
         where
             movie0_.id=? 
             and movie0_.DTYPE='Movie'
     ```

​		

		#### 	단일 테이블 전략의 장점과 단점

- 장점
  - 조인이 필요 없다. 일반적으로 조회 성능이  빠르다.
  - 조회 쿼리가 단순하다
- 단점
  - 예를들어, Movie 엔티티를 저장하고 조회했다고 생각해보자.
  - 그럼 Book의 isbn이나 저자 등등은 null을 허용해야 하는 문제점이 생긴다.
  - 규모가 큰 프로젝트에서는 단일 테이블에 모든 것을 저장하므로 테이블이 커짐에 따라 오히려 조회 성능이 느려질 수 있다.





3. **구현 클래스마다 테이블 전략**

   ![구현클래스마다테이블전략](https://user-images.githubusercontent.com/39195377/97332045-5e01e480-18bd-11eb-98a8-1bcbf0b2480b.PNG)

- 조인 전략과 유사하지만, 슈퍼 타입의 컬럼들을 서브 타입으로 내린다. NAME, PRICE 컬럼들이 중복되도록 허용하는 전략이다.
- 이 전략은 데이터베이스 설계자와 ORM 전문가 둘다 추천하지 않는 전략이다.



- 장점
  - 서브 타입을 명확하게 구분해서 처리할 떄 효과적
  - not null 제약조건 사용 가능
- 단점
  - 여러 자식 테이블을 함께 조회할때 성능이 느림(
  - 자식 테이블을 통합해서 쿼리하기 어려움







### MappedSuperClass

- 객체 입장에서 공통 매핑 정보다 필요할 떄 사용한다

- 예를들어, 수정한 날짜, 수정한 사람 등등

- 이렇게 공통 매핑 정보가 필요할 때, 부모 클래스에 선언하고 속성만 상속 받아서 사용하고 싶을때 사용한다.

- DB 테이블과는 전혀 상관 없다. 

 ![맵드슈퍼](https://user-images.githubusercontent.com/39195377/97332061-5fcba800-18bd-11eb-99a3-b5a40e0f6488.PNG)



​	쉬운 내용이라 코드만 봐도 쉽게 이해할 수 있다.

- 생성자, 생성시간, 수정자, 수성시간 등 모든 엔티티에 공통으로 사용하고 싶은 상황이다.

- 아래 코드와 같이 BaseEntity를 정의한다.

- BaseEntity class는 매핑정보만 상속받는 Superclass라는 의미의 @MappedSuperClass를 선언한다.

  ```java
  @Getter
  @Setter
  @MappedSuperclass
  public abstract class BaseEntity {
  
     private String createdBy;
  
     private LocalDateTime createdDate;
  
     private String lastModifiedBy;
  
     private LocalDateTime lastModifiedDate;
  }
  ```

-  BaseEntity를 상속

  ```java
  @Entity
  public class Member extends BaseEntity {
      ...
  }
  
  @Entity
  public class Team extends BaseEntity {
      ...
  }
  ```

- BaseEntity를 상속받은 엔티티들의 DDL을 살펴보자

  ```sql
  Hibernate: 
      create table Member (
         id bigint generated by default as identity,
          createdBy varchar(255),
          createdDate timestamp,
          lastModifiedBy varchar(255),
          lastModifiedDate timestamp,
          age integer,
          description clob,
          roleType varchar(255),
          name varchar(255),
          locker_id bigint,
          team_id bigint,
          primary key (id)
      )
  Hibernate: 
      create table Team (
         id bigint generated by default as identity,
          createdBy varchar(255),
          createdDate timestamp,
          lastModifiedBy varchar(255),
          lastModifiedDate timestamp,
          name varchar(255),
          primary key (id)
      )
  ...
  ```



	#### MappedSuperClass 정리

- 공통 매핑 정보가 필요할 때 사용한다.
- 상속관계 매핑도 아니고, 엔티티도 아니다.
- 부모 클래스를 상속받는 자식 클래스에게 단순히 **매핑 정보**만 제공할 뿐 조회/검색이 불가능하다.
- 직접 생성해서 사용할 일이 없음으로 추상(abstract)클래스로 만들자.
- 참고로 JPA에서 extends라는 키워드가 허용되는 경우는 딱 두가지이다.
  - @Entity가 붙은 엔티티를 상속받을때
  - @MappedSuperClass를 사용할때.


  </div>
</details>





<details>
  <summary>6. 프록시란?</summary>
  <div markdown="1">
    
  # 프록시

'프록시' 자체를 실무에서 직접 사용할 일은 매우 드물지만, JPA에서 가장 중요한 핵심중 하나인 '지연로딩'과 '즉시로딩' 을 이해하기 위해서는 '프록시'의 개념을 확실히 짚고 넘어가야 한다.

예를들어 Member라는 엔티티와 Team이라는 엔티티가 @ManyToOne으로 관계를 맺고있다고 가정해보자. 그렇다면 Member 엔티티를 조회할때 반드시 Team 엔티티도 조회해야 할까?

- 실제로 필요한 비즈니스 로직에 따라 다르다.
- 비즈니스 로직에서 필요하지 않을 때가 있는데 항상 Team을 함께 가져올 필요는 없다.
- JPA는 이러한 낭비를 지양하기 위해 지연로딩과 '프록시' 를 지원한다.



### 프록시란?

- JPA에서 entityManger.find()와 비슷한 entityManager.getReference() 라는 메서드도 제공한다.
- entityManager.find()는 DB에서 실제로 엔티티 객체를 조회하는 메서드고
- entityManager.getReference()는 DB의 조회를 미루는 가짜 엔티티(프록시) 객체를 조회하는 메서드이다.

```java
//Member 엔티티
@Entity
@Getter
@Setter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Lob
    private String description;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne
    @JoinColumn(name = "locker_id")
    private Locker locker;

    @OneToMany(mappedBy = "member")
    private List<MemberProduct> memberProducts = new ArrayList<>();
}


```

- entityManager.find()로 멤버를 조회하면, 아래와 같이 DB에 쿼리가 바로 실행된다.

```sql
Hibernate: 
    /* insert hello.jpa.Member
        */ insert 
        into
            Member
            (id, createdBy, createdDate, lastModifiedBy, lastModifiedDate, age, description, locker_id, roleType, name) 
        values
            (null, ?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        member0_.id as id1_4_0_,
        member0_.createdBy as createdB2_4_0_,
        member0_.createdDate as createdD3_4_0_,
        member0_.lastModifiedBy as lastModi4_4_0_,
        member0_.lastModifiedDate as lastModi5_4_0_,
        member0_.age as age6_4_0_,
        member0_.description as descript7_4_0_,
        member0_.locker_id as locker_10_4_0_,
        member0_.roleType as roleType8_4_0_,
        member0_.team_id as team_id11_4_0_,
        member0_.name as name9_4_0_,
        locker1_.id as id1_3_1_,
        locker1_.name as name2_3_1_,
        team2_.id as id1_8_2_,
        team2_.createdBy as createdB2_8_2_,
        team2_.createdDate as createdD3_8_2_,
        team2_.lastModifiedBy as lastModi4_8_2_,
        team2_.lastModifiedDate as lastModi5_8_2_,
        team2_.name as name6_8_2_ 
    from
        Member member0_ 
    left outer join
        Locker locker1_ 
            on member0_.locker_id=locker1_.id 
    left outer join
        Team team2_ 
            on member0_.team_id=team2_.id 
    where
        member0_.id=?
findMember.id = 1
findMember.username = creator

```

- 다음은 entityManager.getReference()로 멤버를 조회했을때 DB 쿼리가 실행되는것을 살펴보자.

  ```java
  Member member = new Member();
  member.setCreatedBy("creator");
  
  em.persist(member);
  
  em.flush();
  em.clear();
  
  Member findMember = em.find(Member.class, member.getId());
  System.out.println("findMember.id = " + findMember.getId());
  System.out.println("findMember.username = " + findMember.getUsername());
  
  tx.commit();
  ```

  

  ```sql
  Hibernate: 
      /* insert hello.jpa.Member
          */ insert 
          into
              Member
              (id, createdBy, createdDate, lastModifiedBy, lastModifiedDate, age, description, locker_id, roleType, name) 
          values
              (null, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  findMember = class hello.jpa.Member$HibernateProxy$yJgMgbkR
  findMember.id = 1
  Hibernate: 
      select
          member0_.id as id1_4_0_,
          member0_.createdBy as createdB2_4_0_,
          member0_.createdDate as createdD3_4_0_,
          member0_.lastModifiedBy as lastModi4_4_0_,
          member0_.lastModifiedDate as lastModi5_4_0_,
          member0_.age as age6_4_0_,
          member0_.description as descript7_4_0_,
          member0_.locker_id as locker_10_4_0_,
          member0_.roleType as roleType8_4_0_,
          member0_.team_id as team_id11_4_0_,
          member0_.name as name9_4_0_,
          locker1_.id as id1_3_1_,
          locker1_.name as name2_3_1_,
          team2_.id as id1_8_2_,
          team2_.createdBy as createdB2_8_2_,
          team2_.createdDate as createdD3_8_2_,
          team2_.lastModifiedBy as lastModi4_8_2_,
          team2_.lastModifiedDate as lastModi5_8_2_,
          team2_.name as name6_8_2_ 
      from
          Member member0_ 
      left outer join
          Locker locker1_ 
              on member0_.locker_id=locker1_.id 
      left outer join
          Team team2_ 
              on member0_.team_id=team2_.id 
      where
          member0_.id=?
  findMember.username = creator
  ```

```sql
findMember = class hello.jpa.Member$HibernateProxy$yJgMgbkR
```

이부분을 자세히 보면, HibernateProxy라는 객체를 확인할 수 있다.

- 프록시는 em.find() 를 실행했을때 가짜객체인 프록시를 초기화 한다.
- 실제로 findMember.getId()와 같이 데이터 조회가 필요할때 비로소 실제 쿼리를 실행한다.

![프록시특징1](https://user-images.githubusercontent.com/39195377/97323863-8f29e700-18b4-11eb-9100-4efda21b26c3.PNG)

- 프록시는 실제 클래스를 상속 받아서 만들어진다. 즉 실제 객체의 참조를 보관한다.
- 실제 클래스와 겉 모양이 동일하다.

![위임](https://user-images.githubusercontent.com/39195377/97323856-8df8ba00-18b4-11eb-92d2-cf7234b2dddd.PNG)

- 사용하는 입장에서는 진짜 객체인지 가짜 객체(프록시) 인지 구분하지 않고 사용하면 된다.
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드를 호출한다.



![프록시객체초기화](https://user-images.githubusercontent.com/39195377/97323861-8f29e700-18b4-11eb-9fbe-cc14487dfaaf.PNG)


#### 프록시 객체의 초기화 과정

1. 클라이언트가 getName()을 요청한다.
2. 프록시에서 member target에  값(getName) 이 있는지 확인한다.
3. 값이 없으면 프록시가 영속성 컨텍스트에 요청한다.
4. 영속성 컨텍스트는 DB에서 그 값을 조회한다.
5. 실제 Entity를 생성해서 프록시 member target으로 Entity의 정보를 넘긴다.



### 프록시의 특징 정리

- 프록시는 실제 엔티티로 바뀌는 것이 아니다. 단지 초기화 되면 프록시 객체를 통해서 실제 엔티티에 접근이 가능한 것 뿐.(정확히 말하면 target에 값이 채워지는 것)

- 프록시는 처음 사용할 때 한 번만 초기화 된다.

- 이미 영속성 컨텍스트에 존재하는 값을 조회했다면 프록시로 조회(getReference)해도 원본이 출력된다.

  - 간단하게 생각해보면, 이미 영속성 컨텍스트에 올려져 있는 객체는 DB쿼리문을 실행하지 않아도 되기때문에 굳이 다시 프록시르 감싸서 반환할 필요가 없다.
  - JPA는 하나의 영속성 컨텍스트에서 조회하는 같은 엔티티의 동일성을 보장한다.

  ```java
  Member find = em.find(Member.class, member.getId());
  Member reference = em.getReference(Member.class, member.getId());
  
  System.out.println("find == reference : " + (find == reference)); // true
  ```

  



정리

★프록시는 단순히 DB조회를 필요한 시점까지 미루는 개념으로, 실제로 조회가 필요할 때 쿼리문이 나간다.

★ **이렇게 복잡한 작업을 JPA가 내부적으로 다 처리해주지만, 우리가 실무에서 직접 개발을 할 때는 이 객체가 프록시인지, 진짜 객체인지 신경쓸 필요가 없다. 그냥 개발하자.**


  </div>
</details>


<details>
  <summary>7. 지연로딩과 즉시로딩 </summary>
  <div markdown="1">

# 즉시로딩과 지연로딩

- 프록시에서 공부했던 내용을 생각해보자.
- Member를 조회할때, 연관관계를 맺고있는 Team까지 꼭 함께 조회해야 할까?
- 단순히 Member만 필요한데 Team도 함께 조회하면 분명한 낭비이다.
- 이러한 낭비를 해결하기 위해  JPA는 '지연로딩'을 지원한다.





#### 지연로딩을 사용해서 Member 조회하기

- Member와 Team은 @ManyToOne (다대일) 관계로 매핑이 되어있음

- FetchType.LAZY가 바로 지연로딩 설정이다.

  ```java
  @Entity
  public class Member  {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
  
      @Column(name = "name")
      private String username;
  
      private Integer age;
  
      @Enumerated(EnumType.STRING)
      private RoleType roleType;
  
      @Lob
      private String description;
  
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "team_id", insertable = false, updatable = false)
      private Team team;
  
      @OneToOne
      @JoinColumn(name = "locker_id")
      private Locker locker;
  
      @OneToMany(mappedBy = "member")
      private List<MemberProduct> memberProducts = new ArrayList<>();
  
      public void changeTeam(Team team) {
          this.team = team;
          this.team.getMembers().add(this);
      }
  }
  ```

- 메인 함수에서 팀과 멤버를 저장하고 조회해보자.

  ```java
  Team team = new Team();
  team.setName("teamA");
  em.persist(team);
  
  Member member = new Member();
  member.setUsername("memberA");
  em.persist(member);
  
  member.changeTeam(team);
  
  em.flush();
  em.clear();
  
  Member findMember = em.find(Member.class, member.getId());
  
  System.out.println(findMember.getTeam().getClass());
  ```

  - Member를 조회하고, Team 객체의 클래스를 확인해보면 프록시 객체가 조회된다.
  - 쿼리문도 Member 조회 쿼리만 실행된다.

- 다음으로, 팀의 이름을 출력해보자

  - 이제 Team을 실제로 사용하는 시점이 되었기 때문에 조회 쿼리가 이제서야 나간다.

  ```java
  Team team = new Team();
  team.setName("teamA");
  em.persist(team);
  
  Member member = new Member();
  member.setUsername("memberA");
  em.persist(member);
  
  member.changeTeam(team);
  
  em.flush();
  em.clear();
  
  Member findMember = em.find(Member.class, member.getId());
  
  System.out.println(findMember.getTeam().getClass());
  System.out.println("TEAM NAME : " + findMember.getTeam().getName());
  ```

  - 실행된 쿼리

    ```sql
    Hibernate: 
        select
            member0_.id as id1_4_0_,
            member0_.createdBy as createdB2_4_0_,
            member0_.createdDate as createdD3_4_0_,
            member0_.lastModifiedBy as lastModi4_4_0_,
            member0_.lastModifiedDate as lastModi5_4_0_,
            member0_.age as age6_4_0_,
            member0_.description as descript7_4_0_,
            member0_.locker_id as locker_10_4_0_,
            member0_.roleType as roleType8_4_0_,
            member0_.team_id as team_id11_4_0_,
            member0_.name as name9_4_0_,
            locker1_.id as id1_3_1_,
            locker1_.name as name2_3_1_ 
        from
            Member member0_ 
        left outer join
            Locker locker1_ 
                on member0_.locker_id=locker1_.id 
        where
            member0_.id=?
            
    class hello.jpa.Team$HibernateProxy$z4JtUeLD  // 프록시 객체
    ​
    Hibernate: 
        select
            team0_.id as id1_8_0_,
            team0_.createdBy as createdB2_8_0_,
            team0_.createdDate as createdD3_8_0_,
            team0_.lastModifiedBy as lastModi4_8_0_,
            team0_.lastModifiedDate as lastModi5_8_0_,
            team0_.name as name6_8_0_ 
        from
            Team team0_ 
        where
            team0_.id=?
            
    TEAM NAME : teamA
    ```

  - 방금 코드로 공부한 내용을 정리하자면 아래 그림과 같다.

![지연로딩 정리](https://user-images.githubusercontent.com/39195377/97402257-9ba25400-1935-11eb-8092-beb19f3ddf3d.PNG)

  

  ##### 지연로딩의 내부 매커니즘

  - 로딩되는 시점에 Lazy 로딩 설정이 되어있는 Team 엔티티는 프록시 객체로 가져온다.
  - 이후 실제로 Team 객체를 사용하는 시점에 초기화되면서 DB 쿼리가 실행된다.
    - getTeam().getName()으로 조회시 쿼리가 나간다.
    - getName을 조회하기 전에 getTeam으로 Team을 조회하면 프록시 객체가 조회된다.

  ![지연로딩](https://user-images.githubusercontent.com/39195377/97402262-9cd38100-1935-11eb-931d-0fa6b29c5c55.PNG)

  

  

  

  ### 즉시 로딩을 사용해서 Member 조회하기

  - 대부분의 비즈니스 로직에서 Member와 Team을 함께 사용해야 할 때 즉시로딩을 사용하자.

  - fetch 타입을 EAGER로 설정하면 된다.  ex) @ManyToOne(fetch = FetchType.EAGER)

  - 이렇게 하면 실제로 조회할 때 한방 쿼리로 전부 조회해온다. (실제 Team을 사용할 때 쿼리가 안나가도 된다.)

  - 실행 결과를 보자. Team도 프록시 객체가 아닌 실제 객체이다.

    

  ```java
  public class Member extends BaseEntity {
      ...
      @ManyToOne(fetch = FetchType.EAGER)
      @JoinColumn(name = "team_id", insertable = false, updatable = false)
      private Team team;
      ...
  }
  ```

  

  ```java
  Team team = new Team();
  team.setName("teamA");
  em.persist(team);
  
  Member member = new Member();
  member.setUsername("memberA");
  em.persist(member);
  
  member.changeTeam(team);
  
  em.flush();
  em.clear();
  
  Member findMember = em.find(Member.class, member.getId());
  
  System.out.println(findMember.getTeam().getClass());
  System.out.println("TEAM NAME : " + findMember.getTeam().getName());
  tx.commit();
  ```

  실행 쿼리

  ```sql
  Hibernate: 
      select
          member0_.id as id1_4_0_,
          member0_.createdBy as createdB2_4_0_,
          member0_.createdDate as createdD3_4_0_,
          member0_.lastModifiedBy as lastModi4_4_0_,
          member0_.lastModifiedDate as lastModi5_4_0_,
          member0_.age as age6_4_0_,
          member0_.description as descript7_4_0_,
          member0_.locker_id as locker_10_4_0_,
          member0_.roleType as roleType8_4_0_,
          member0_.team_id as team_id11_4_0_,
          member0_.name as name9_4_0_,
          locker1_.id as id1_3_1_,
          locker1_.name as name2_3_1_,
          team2_.id as id1_8_2_,
          team2_.createdBy as createdB2_8_2_,
          team2_.createdDate as createdD3_8_2_,
          team2_.lastModifiedBy as lastModi4_8_2_,
          team2_.lastModifiedDate as lastModi5_8_2_,
          team2_.name as name6_8_2_ 
      from
          Member member0_ 
      left outer join
          Locker locker1_ 
              on member0_.locker_id=locker1_.id 
      left outer join
          Team team2_ 
              on member0_.team_id=team2_.id 
      where
          member0_.id=?
  class hello.jpa.Team
  TEAM NAME : teamA
  
  ```

  

  #### 가급적 즉시 로딩을 사용하지 말자.

  - 실무에서는 가급적 지연 로딩(LAZY)만 사용하도록 해야한다.

  - 즉시로딩을 사용하면 예상치 못한 쿼리가 발생한다.

    - 예를들어, 클라이언트는 분명 Member만 조회했을 뿐인데 join 쿼리가 함께 실행된다.
    - 만약 @ManyToOne 연관관계 매핑이 5개가 있는데 전부 즉시 로딩(EAGER)라고 생각해보자.
    - Join이 5번 일어난다. 실무에서는 더 많은 테이블이 존재하는데, 엄청난 낭비이다.

  - 즉시로딩은 JPQL을 사용할떄 **N+1 문제**를 일으킨다.

    - N+1 문제란 ? 
    - 예를들어, 'Select m from Member m' 으로 조회하면 당연히 Member만 select 된다.
    - Member를 전부 조회하고 보니까 member와 연관관계를 맺은 Team의 fetchType이 EAGER인 것을 확인했다.
    - LAZY면 프록시 객체를 넣으면 되겠지만, EAGER 타입은 반환하는 시점에 모두 조회가 끝나있어야 한다.
    - 따라서 Member를 다 가져오고 나서, 그 Member와 연관되어있는 Team을 모두 다시 가져온다.

    - JPQL의 N+1 문제를 아래 코드로 이해해보자.

    ```java
    Team team1 = new Team();
    team1.setName("teamA");
    em.persist(team1);
    
    Team team2 = new Team();
    team2.setName("teamB");
    em.persist(team2);
    
    Member member1 = new Member();
    member1.setUsername("memberA");
    em.persist(member1);
    member1.changeTeam(team1);
    
    Member member2 = new Member();
    member2.setUsername("memberB");
    em.persist(member2);
    member2.changeTeam(team2);
    
    em.flush();
    em.clear();
    
    List<Member> members = em
                    .createQuery("select m from Member m", Member.class)
      .getResultList();
    ​
    tx.commit();
    ```

    위 코드를 실행한 결과 쿼리를 보자.

    ```sql
    Hibernate: 
        /* select
            m 
        from
            Member m */ select
                member0_.id as id1_4_,
                member0_.createdBy as createdB2_4_,
                member0_.createdDate as createdD3_4_,
                member0_.lastModifiedBy as lastModi4_4_,
                member0_.lastModifiedDate as lastModi5_4_,
                member0_.age as age6_4_,
                member0_.description as descript7_4_,
                member0_.locker_id as locker_10_4_,
                member0_.roleType as roleType8_4_,
                member0_.team_id as team_id11_4_,
                member0_.name as name9_4_ 
            from
                Member member0_
    Hibernate: 
        select
            team0_.id as id1_8_0_,
            team0_.createdBy as createdB2_8_0_,
            team0_.createdDate as createdD3_8_0_,
            team0_.lastModifiedBy as lastModi4_8_0_,
            team0_.lastModifiedDate as lastModi5_8_0_,
            team0_.name as name6_8_0_ 
        from
            Team team0_ 
        where
            team0_.id=?
    Hibernate: 
        select
            team0_.id as id1_8_0_,
            team0_.createdBy as createdB2_8_0_,
            team0_.createdDate as createdD3_8_0_,
            team0_.lastModifiedBy as lastModi4_8_0_,
            team0_.lastModifiedDate as lastModi5_8_0_,
            team0_.name as name6_8_0_ 
        from
            Team team0_ 
        where
            team0_.id=?
    ```

  - 일단 먼저 Member 를 조회해서 가져온다.
  - 그리고나서 Team 객체를 조회하는 쿼리를 날린다.
  - **만약 멤버수가 수천 수만명이라고 가정해보자. 추가적으로 Team을 조회하는 쿼리가 수천 수만개가 나가는 것이다.**
  - 즉 N+1 문제의 의미는, Member 조회 쿼리는 1개 날렸는데, 그것때문에 추가 쿼리가 N개 나간다는 의미이다.

  

  

  #### 정리

  1. 실무에서는 모두 LAZY 전략을 사용하자.
     - 이 경우, 만약 대부분 비즈니스 로직이 Member와 Team을 함께 사용한다면 ?
     - 이러한 문제를 해결하기 위해 JPA에서는 Fecth Join과 @EntityGreaph, 배치 사이즈 설정 이라는 것을 지원한다. 추후에 학습해보자.
  2. @ManyToOne, @OneToOne과 같이 @XXXToOnesms FetchType의 Default값이 EAGER이다.
     - 따라서, 위 두 전략은 반드시 LAZY로 명시적으로 설정을  해줘야 한다.
  3. 반대로 @XXXToMany는 FetchType의 Default값이 LAZY다. 손댈필요 없다.
  4. Member와 Team을 자주 함께 사용한다 -> 즉시로딩 / Member와 Team을 가끔 함께 사용한다 -> 지연로딩
     - **위와 같은 내용은 매우 이론적인 이야기다. 그냥 LAZY로 사용하자**
     - 즉시 로딩은 상상하지 못한 쿼리가 나간다.

  

  
  </div>
</details>









