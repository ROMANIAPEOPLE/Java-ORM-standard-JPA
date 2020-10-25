# Java-ORM-standard-JPA
JAPA ORM 표준 JPA 프로그래밍 이론편

<details>
  <summary>1.영속성 컨텍스트</summary>
  <div markdown="1">
  # 자바 ORM 표준 JPA 프로그래밍

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
  <summary>1. 객체와 DB의 기본적인 매핑 방법</summary>
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
  </div>
</details>




