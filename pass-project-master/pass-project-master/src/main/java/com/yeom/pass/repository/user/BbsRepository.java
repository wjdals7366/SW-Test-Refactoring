package com.yeom.pass.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BbsRepository extends JpaRepository<Bbs, Long> {
    @Override
    List<Bbs> findAll();
    @Override
    @EntityGraph(attributePaths = {"comments", "user"})   // comments, user fetch join
    Page<Bbs> findAll(Pageable pageable);

    @Query("SELECT DISTINCT b FROM Bbs b LEFT JOIN FETCH b.comments WHERE b.id = :bbsId")
        // 기본적으로 JPA 는 연관된 엔티티를 지연 로딩하므로 연관된 엔티티를 가져오려 할 때 추가적인 쿼리가 실행되어야함
        // -> JOIN FETCH 을 통한 N+1 문제 해결, 일반 join 은 연관 Entity 까지 초기화하지 않음
    Bbs findByIdWithComments(@Param("bbsId") Long bbsId);

    @Query("SELECT b FROM Bbs b LEFT JOIN FETCH b.comments WHERE b.Title LIKE %:title%")
    List<Bbs> findByTitleWithComments(@Param("title") String title);

//    @Query("SELECT b FROM Bbs b LEFT JOIN FETCH b.comments WHERE b.Title LIKE %:title%")
//    Page<Bbs> findByTitleWithComments2(@Param("title") String title, Pageable pageable); list->page 변환없이 가져오는 경우

    @Query("SELECT b FROM Bbs b LEFT JOIN FETCH b.comments WHERE b.Content LIKE %:content%")
    List<Bbs> findByContentWithComments(@Param("content") String content);

    @Query("SELECT b FROM Bbs b LEFT JOIN FETCH b.comments WHERE b.user.userName LIKE %:writer%")
    List<Bbs> findByWriterWithComments(@Param("writer") String writer);



}