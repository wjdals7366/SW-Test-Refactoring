package com.yeom.pass.service.user;

import com.yeom.pass.repository.user.Bbs;
import com.yeom.pass.repository.user.BbsRepository;
import com.yeom.pass.repository.user.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EagerService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BbsRepository bbsRepository;


    public Bbs getBbs(Long id){
        Bbs bbs = bbsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

//        Hibernate.initialize(bbs.getComments());  // 강제 초기화 대신 Fetch join 을 활용했으므로 주석 처리
        return bbs;
    }

    public List<Bbs> getBbsListWithEagerComments(String name, String keyword){

        List<Bbs> bbsList;

        if(name.equals("Title")){   // Title 로 검색할 경우
            bbsList = bbsRepository.findByTitleWithComments(keyword);   // join fetch comments
        }

        else if (name.equals("Writer")) {
            bbsList = bbsRepository.findByWriterWithComments(keyword);
        }

        else{
            bbsList = bbsRepository.findByContentWithComments(keyword);
        }

        return bbsList;
    }


    public Page<Bbs> getPagedBbsWithEagerComments(PageRequest pageable){
        return bbsRepository.findAll(pageable);
    }



}
