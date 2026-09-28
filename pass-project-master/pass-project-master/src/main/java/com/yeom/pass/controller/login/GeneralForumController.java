package com.yeom.pass.controller.login;


import com.yeom.pass.repository.user.Bbs;
import com.yeom.pass.repository.user.BbsDto;
import com.yeom.pass.repository.user.Comment;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.service.user.BbsService;
import com.yeom.pass.service.user.EagerService;
import com.yeom.pass.service.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/generalforum")
public class GeneralForumController {

    @Autowired
    private BbsService bbsService;
    @Autowired
    private HttpSession session;

    @Autowired
    private EagerService eagerService;


    @RequestMapping({"/", ""})
    public String generalForum(@RequestParam(defaultValue = "1") int page, Model model, BbsDto.SearchRequest request){

        boolean searchExist = false;    // 검색 유무
        List<Integer> pList = new ArrayList<>();

        int fPage = page - (page - 1) % 5;    // 1~5 -> 1 , 6~10 -> 6

        for(int i = fPage; i <= page; i++){
            pList.add(i);   // 현재 index(p)까지 표시}
        }

        for(int i = page; i < fPage + 4; i++){
            if(bbsService.getBbsList(request, i).hasNext())   // 다음 페이지에 게시글이 존재할 시 index 표시
                pList.add(i + 1);
            else
                break;
        }

        model.addAttribute("bbsList", bbsService.getBbsList(request, page));   // 현재 페이지 게시글 list
        model.addAttribute("pageIndexList", pList);


        if(fPage != 1)
            model.addAttribute("preIndex", fPage - 5);  // 이전 페이지

        if(bbsService.getBbsList(request, fPage + 4).hasNext())
            model.addAttribute("nextIndex", fPage + 5); // 다음 페이지

        if(request.getKeyword() != null && !(request.getKeyword().equals(""))) {
            String name = request.getName();
            String keyword = request.getKeyword();
            searchExist = true;

            model.addAttribute("searchExist", searchExist);
            model.addAttribute("name", name);
            model.addAttribute("keyword", keyword);
        }   // 검색 내용이 있을 시


        return "/board/generalforum";
    }

    @RequestMapping("/bbs_view")
    public String bbsView(@RequestParam Long bbs_id, Model model){

        Bbs bbs = eagerService.getBbs(bbs_id); // 메소드 종료 후 bbs 는 준영속 상태가 됨

        List<Comment> comments = bbsService.getComments(bbs_id);

        model.addAttribute("bbs", bbs);
        model.addAttribute("comments", comments);
        model.addAttribute("cmtCount", comments.size());

        return "/board/bbs_view";
    }

    @RequestMapping("/write")
    public String bbsWrite(){

        return "/board/write";
    }

    @RequestMapping("/writeAction")
    public String bbsWriteAction(RedirectAttributes re, BbsDto.PostRequest request){
        UserEntity user = (UserEntity)session.getAttribute("user");

        if(bbsService.insertBbs(request,user.getUserId()))
            return "redirect:/generalforum";

        else {
            re.addFlashAttribute("msg", "입력이 안된 사항이 있습니다.");
            return "redirect:/generalforum/write";
        }
    }

    @RequestMapping("/bbs_update")
    public String bbsUpdate(@RequestParam Long bbs_id, Model model, RedirectAttributes re){

        UserEntity user = (UserEntity)session.getAttribute("user");

        if(user == null) {
            re.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        String sessionID = user.getUserId();

        if(!sessionID.equals(bbsService.getUserIdByBbsId(bbs_id))){
            re.addFlashAttribute("msg", "권한이 없습니다");
            return "redirect:/generalforum/bbs_view?bbs_id="+bbs_id;
        }

        Bbs bbs = bbsService.getBbs(bbs_id);

        model.addAttribute("bbs", bbs);

        return "/board/bbs_update";
    }

    @RequestMapping("/bbs_updateAction")
    public String bbsUpdateAction(BbsDto.UpdateRequest request, RedirectAttributes re){

        if(bbsService.updateBbs(request))
            return "redirect:bbs_view?bbs_id=" + request.getBbs_id();

        else {
            re.addFlashAttribute("msg", "입력이 안된 사항이 있습니다.");
            return "redirect:/generalforum/bbs_update?bbs_id=" + request.getBbs_id();
        }

    }

    @RequestMapping("/bbs_delete")
    public String bbsDelete(@RequestParam Long bbs_id, RedirectAttributes re){

        UserEntity user = (UserEntity)session.getAttribute("user");

        if(user == null) {
            re.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        String sessionID = user.getUserId();

        if(!sessionID.equals(bbsService.getUserIdByBbsId(bbs_id))){
            re.addFlashAttribute("msg", "권한이 없습니다");
            return "redirect:/generalforum/bbs_view?bbs_id="+bbs_id;
        }

        bbsService.deleteBbs(bbs_id);

        return "redirect:/generalforum";
    }


    @RequestMapping("/commentAction")
    public String commentAction(BbsDto.CommentRequest request, Model model, RedirectAttributes re){

        model.addAttribute("bbsId", request.getBbs_id());

        UserEntity user = (UserEntity) session.getAttribute("user");

        if(user == null) {
            re.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:bbs_view?bbs_id=" + request.getBbs_id();
        }

        if(bbsService.insertComment(request, user))
            return "redirect:bbs_view?bbs_id=" + request.getBbs_id();
        else {
            re.addFlashAttribute("msg", "내용을 입력해 주세요.");
            return "redirect:bbs_view?bbs_id=" + request.getBbs_id();
        }

    }

    @RequestMapping("/comment_delete")
    public String commentDelete(@RequestParam Long bbs_id, @RequestParam Long comment_id, RedirectAttributes re){

        UserEntity user = (UserEntity)session.getAttribute("user");

        if(user == null) {
            re.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        String sessionID = user.getUserId();

        if(!sessionID.equals(bbsService.getUserIdByCommentId(comment_id))){
            re.addFlashAttribute("msg", "권한이 없습니다");
            return "redirect:/generalforum/bbs_view?bbs_id="+ bbs_id;
        }

        bbsService.deleteComment(comment_id);

        return "redirect:/generalforum/bbs_view?bbs_id=" + bbs_id;

    }
}
