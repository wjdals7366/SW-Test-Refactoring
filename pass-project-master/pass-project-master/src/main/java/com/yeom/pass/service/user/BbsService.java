package com.yeom.pass.service.user;

import com.yeom.pass.repository.user.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BbsService {
    @Autowired
    BbsRepository bbsRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EagerService eagerService;

    @Autowired
    ModelMapper modelMapper;


    public Bbs getBbs(Long id){
        Bbs bbs = bbsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return bbs;
    }

    public Page<Bbs> listToPage(List<Bbs> bbsList, PageRequest pageable){

        Page<Bbs> bbs = new PageImpl<>(bbsList, pageable, bbsList.size());

        return bbs;
    }
    public Page<Bbs> getBbsList(BbsDto.SearchRequest request, int p){

        Page<Bbs> bbsPage;

        PageRequest pageable = PageRequest.of(p - 1,10, Sort.by(Sort.Direction.DESC, "id"));

        if(request.getKeyword() != null && !(request.getKeyword().equals(""))) {
            String name = request.getName();
            String keyword = request.getKeyword();

            List<Bbs> bbsList = eagerService.getBbsListWithEagerComments(name, keyword);

            //bbsPage = bbsRepository.findAll(pageable);

            bbsPage = listToPage(bbsList, pageable);   // List -> Page 변환

            for(Bbs bbs : bbsPage){
                bbs.setDatetime(bbs.getCreatedAt().toString().replace("T", " "));
            }

            return bbsPage;

        }

        bbsPage = eagerService.getPagedBbsWithEagerComments(pageable);

        for(Bbs bbs : bbsPage){
            bbs.setDatetime(bbs.getCreatedAt().toString().replace("T", " "));
        }

        return bbsPage;
    }

    @Transactional
    public boolean insertBbs(BbsDto.PostRequest request, String userID) {   // 게시판 글 등록

        if(request.getTitle().isEmpty() || request.getContent().isEmpty())
            return false;

        else {
            Bbs bbs = modelMapper.map(request, Bbs.class);

            UserEntity user = userRepository.findByUserId(userID);

            //bbs.setCreatedAt(LocalDateTime.now());

            bbs.setUser(user);

            bbsRepository.save(bbs);

            return true;
        }
    }

    public String getUserIdByBbsId(Long bbs_id){
        return bbsRepository.findById(bbs_id).get().getUser().getUserId();
    }

    public String getUserIdByCommentId(Long comment_id) {return commentRepository.findById(comment_id).get().getUser().getUserId();}

    public boolean updateBbs(BbsDto.UpdateRequest request) {

        if(request.getTitle().isEmpty() || request.getContent().isEmpty())
            return false;


        else {
            Long bbsId = request.getBbs_id();
            String title = request.getTitle();
            String content = request.getContent();

            Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(() -> new IllegalArgumentException("게시글 수정 실패: 해당 게시글이 존재하지 않습니다."));
            bbs.setTitle(title);
            bbs.setContent(content);

            try {
                if(bbs != null) {
                    bbsRepository.save(bbs); // 게시판 수정
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }


    public void deleteBbs(Long bbsId) {

        Bbs bbs = bbsRepository.findById(bbsId).orElse(null);

        if(bbs != null){
            try {
                bbsRepository.delete(bbs);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public List<Comment> getComments(Long id){  // @Transactional 로 감싸 proxy 객체를 사용하는 방법과 Fetch join 을 사용하여 실제 객체를 가져오는 방법 2가지 사용가능

        Bbs bbs = bbsRepository.findByIdWithComments(id);   // @Transactional 이 없으면 Fetch join 을 사용하여 실제 Comment 객체를 함께 로드해야함
        List<Comment> comments = bbs.getComments(); // Fetch join 을 사용하면 proxy 가 아닌 실제 객체를 가져오게 됨

        for(Comment c : comments){  // @Transactional 를 사용하게 될 경우, DB 로부터 로드되어 트랜잭션 범위 내에서 프록시 객체(지연 로딩된 comments)가 초기화됨 = 실제 엔티티로 로딩
            c.setDatetime(c.getCreatedAt().toString().replace("T", " "));
        }

        return comments;
    }

    @Transactional
    public boolean insertComment(BbsDto.CommentRequest request, UserEntity user) {

        if(request.getBody().isEmpty())
            return false;

        else{
            Bbs bbs = bbsRepository.findById(request.getBbs_id()).orElseThrow(() -> new IllegalArgumentException("댓글 쓰기 실패: 해당 게시글이 존재하지 않습니다."));

            Comment comment = modelMapper.map(request, Comment.class);
            comment.setUser(user);
            comment.setNickname(user.getUserName());
            comment.setBbs(bbs);
            //comment.setCreatedAt(LocalDateTime.now());

            try {
                commentRepository.save(comment);
            }catch (Exception e){
                e.printStackTrace();
            }

            return true;
        }
    }

    public void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        if(comment != null){
            try {
                commentRepository.delete(comment);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

