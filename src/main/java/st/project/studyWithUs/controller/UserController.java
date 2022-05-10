package st.project.studyWithUs.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import st.project.studyWithUs.argumentresolver.Login;
import st.project.studyWithUs.domain.User;
import st.project.studyWithUs.domain.UserTeam;
import st.project.studyWithUs.interceptor.SessionConst;
import st.project.studyWithUs.service.pointInfo.PointInfoService;
import st.project.studyWithUs.service.teamService.TeamService;
import st.project.studyWithUs.service.userService.UserService;
import st.project.studyWithUs.service.userTeamService.UserTeamService;
import st.project.studyWithUs.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PointInfoService pointInfoService;
    private final UserTeamService userTeamService;
    private final TeamService teamService;

    @GetMapping("/")
    public String home(){
        return "home";
    }

    @GetMapping("/myPage")
    public String myPage(Model model, @Login User loginUser){
        User user = userService.find(loginUser.getUID());
        model.addAttribute("loginUser", user);
        return "myPage";
    }

    @GetMapping("/chargePoint")
    public String chargePoint(){
        return "chargePoint";
    }


    @ResponseBody
    @GetMapping("/deposit")
    public void deposit(Long point, @Login User loginUser){
        userService.updatePoint(point, loginUser.getUID());
        pointInfoService.deposit(point, 1L );
    }

    @GetMapping("/editUser")
    public String editUser(){
        return "editUser";
    }

    @ResponseBody
    @PostMapping("/saveEditUser")
    public boolean saveEditUser(@RequestParam String photo,
                                @RequestParam String name,
                                @RequestParam String id,
                                @RequestParam String pw,
                                @RequestParam String email,
                                @Login User loginUser,
                                HttpServletRequest request){


        userService.saveEditUser(loginUser.getUID(), photo, name, id, pw, email);
        loginUser = userService.findByuID(loginUser.getUID());

        HttpSession session = request.getSession();
        // 세션에 LOGIN_USER라는 이름(SessionConst.class에 LOGIN_USER값을 "loginUser")을 가진 상자에 loginUser 객체를 담음.
        // 즉, 로그인 회원 정보를 세션에 담아놓는다.
        session.setAttribute(SessionConst.LOGIN_USER, loginUser);
        return true;

    }

    @ResponseBody
    @GetMapping("/getUserInfo")
    public UserVO getUserInfo(@Login User loginUser){
        UserVO userVO = new UserVO();
        userVO.setUserID(loginUser.getUserID());
        userVO.setUserName(loginUser.getUserName());
        userVO.setPw(loginUser.getPassword());
        userVO.setEmail(loginUser.getEmail());
        userVO.setUserImage(loginUser.getUserImage());
        return userVO;
    }

    //포인트 충전 로직
    @ResponseBody
    @GetMapping("/studyDeposit")
    public boolean studyDeposit( @RequestParam Long tId , @RequestParam Long point, @Login User loginUser){


        if(userService.checkPoint(point, loginUser.getUID())==true){ //현재 유저가 들고있는 포인트로 참여할 수 있다면,
            teamService.increaseData(tId, point); //팀의 현재 인원 및 보증금 올리기.
            UserTeam userTeam = new UserTeam();
            userTeam.setTeam(teamService.findBytID(tId));
            userTeam.setUser(userService.find(loginUser.getUID()));
            userTeam.setExist(false);
            userTeam.setRealTime(0L);
            userTeam.setTotalTime(0L);
            userTeamService.save(userTeam);
            return true;
        }
        return false;
    }

    @ResponseBody
    @GetMapping("/findUser")
    public UserVO findUser(@Login User loginUser){
        UserVO uservo = new UserVO();
        uservo.setUserName(loginUser.getUserName());
        uservo.setPoint(loginUser.getPoint());
        return uservo;
    }
}
