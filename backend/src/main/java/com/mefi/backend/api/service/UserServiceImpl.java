package com.mefi.backend.api.service;

import com.mefi.backend.api.request.*;
import com.mefi.backend.api.response.MemberResDto;
import com.mefi.backend.api.response.UserModifyAllResDto;
import com.mefi.backend.common.exception.ErrorCode;
import com.mefi.backend.common.exception.Exceptions;
import com.mefi.backend.db.entity.Token;
import com.mefi.backend.db.entity.User;
import com.mefi.backend.db.repository.TokenRepository;
import com.mefi.backend.db.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenRepository tokenRepository;
    private final FileService fileService;

    // 회원가입
    @Override
    @Transactional
    public void join(JoinReqDto joinReqDto) {
        
        // 가입 정보 입력
        User user = User.builder()
                .email(joinReqDto.getEmail())
                .password(bCryptPasswordEncoder.encode(joinReqDto.getPassword()))
                .name(joinReqDto.getName())
                .position(joinReqDto.getPosition())
                .dept(joinReqDto.getDept())
                .build();

        // DB 저장
        userRepository.save(user);
    }

    // 회원탈퇴
    @Override
    @Transactional
    public void withdraw(User user, UserWithdrawReqDto userWithdrawReqDto) {
        
        // 본인 인증을 위한 현재 비밀번호 확인
        if(!bCryptPasswordEncoder.matches(
                userWithdrawReqDto.getCurrentPassword(),user.getPassword())) {
            throw new Exceptions(ErrorCode.CORRECT_NOT_PASSWORD);
        }

        // 유저 토큰 삭제
        
        // 유저 토큰 조회
        if(!tokenRepository.findByUserId(user.getId()).isPresent()) {
            throw new Exceptions(ErrorCode.TOKEN_NOT_EXIST);
        }

        Token token = tokenRepository.findByUserId(user.getId()).get();
        tokenRepository.delete(token);

        // 유저 삭제
        userRepository.delete(user);
    }

    // 식별 ID로 유저 조회
    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException());
    }

    // 검색어로 유저 조회
    @Override
    public List<MemberResDto> getSearchUsers(String keyword) {
        return userRepository.findByKeyWord(keyword);
    }

    // 회원 정보 전체 수정
    @Override
    @Transactional
    public UserModifyAllResDto modifyUserInfoAll(Long id, UserModifyAllReqDto userModifyAllReqDto, MultipartFile profileImg) throws IOException {

        // 유저 조회
        if (!userRepository.findById(id).isPresent())
            throw new Exceptions(ErrorCode.USER_NOT_EXIST);

        User user = userRepository.findById(id).get();

        // 변경할 프로필 이미지 존재하는지 확인
        String imgUrl = userModifyAllReqDto.getImgUrl();
        log.info("Updated Profile Image : {}", profileImg.getOriginalFilename());

        if(profileImg!=null){
            // 기존 이미지 삭제
            fileService.deleteProfile(user.getImgUrl());

            // 새로운 이미지 업로드
            imgUrl = fileService.createProfile(profileImg);
        }

        
        // 전체 수정
        user.updateAll(userModifyAllReqDto.getName(),
                userModifyAllReqDto.getDept(),
                userModifyAllReqDto.getPosition(),
                imgUrl);

        // 변경된 유저 정보 Dto에 담기
        UserModifyAllResDto userModifyAllResDto = new UserModifyAllResDto(
                user.getEmail(),user.getName(),user.getDept(),user.getPosition(),user.getImgUrl());

        return userModifyAllResDto;
    }

    // 회원 정보 부분 수정
    @Override
    @Transactional
    public void modifyUserInfo(Long id, UserModifyReqDto userModifyReqDto) {

        // 유저 조회
        if (!userRepository.findById(id).isPresent())
            throw new Exceptions(ErrorCode.USER_NOT_EXIST);

        User user = userRepository.findById(id).get();

        // 항목에 맞게 수정
        if("name".equals(userModifyReqDto.getCategory()))
            user.updateName(userModifyReqDto.getContent());

        else if("password".equals(userModifyReqDto.getCategory())) {

            // 이전 비밀번호와 동일한 경우
            if(user.getPassword().equals(bCryptPasswordEncoder.encode(userModifyReqDto.getContent())))
                throw new Exceptions(ErrorCode.SAME_AS_BEFORE);

            user.updatePassword(bCryptPasswordEncoder.encode(userModifyReqDto.getContent()));
        }


        else if("dept".equals(userModifyReqDto.getCategory()))
            user.updateDept(userModifyReqDto.getContent());

        else if("position".equals(userModifyReqDto.getCategory()))
            user.updatePosition(userModifyReqDto.getContent());

        else if("imgUrl".equals(userModifyReqDto.getCategory()))
            user.updateImgUrl(userModifyReqDto.getContent());

        // 항목이 존재하지 않는 경우
        else throw new Exceptions(ErrorCode.CATEGORY_NOT_EXIST);
    }

    // 회원 기본 비밀번호 수정
    @Override
    @Transactional
    public void modifyUserPassword(Long id, UserModifyPasswordReqDto userModifyPasswordReqDto) {

        // 유저 조회
        if(!userRepository.findById(id).isPresent())
            throw new Exceptions(ErrorCode.USER_NOT_EXIST);

        User user = userRepository.findById(id).get();

        // 본인 인증을 위한 현재 비밀번호 확인
        if(!bCryptPasswordEncoder.matches(
                userModifyPasswordReqDto.getCurrentPassword(),user.getPassword())) {
            throw new Exceptions(ErrorCode.CORRECT_NOT_PASSWORD);
        }

        // 이전 비밀번호와 다른지 확인
        if(bCryptPasswordEncoder.matches(
                userModifyPasswordReqDto.getModifyPassword(),user.getPassword())) {
            throw new Exceptions(ErrorCode.SAME_AS_BEFORE);
        }
        
        // 비밀번호 수정
        user.updatePassword(bCryptPasswordEncoder.encode(userModifyPasswordReqDto.getModifyPassword()));
    }

    // 찾기 인증 후 비밀번호 수정
    @Override
    @Transactional
    public void recoveryUserPassword(UserPasswordRecoveryReqDto userPasswordRecoveryReqDto) {

        // 유저 조회
        if(!userRepository.findByEmail(userPasswordRecoveryReqDto.getEmail()).isPresent())
            throw new Exceptions(ErrorCode.USER_NOT_EXIST);

        User user = userRepository.findByEmail(userPasswordRecoveryReqDto.getEmail()).get();

        // 이전 비밀번호와 다른지 확인
        if(bCryptPasswordEncoder.matches(
                userPasswordRecoveryReqDto.getModifyPassword(),user.getPassword())) {
            throw new Exceptions(ErrorCode.SAME_AS_BEFORE);
        }

        // 비밀번호 수정
        user.updatePassword(bCryptPasswordEncoder.encode(userPasswordRecoveryReqDto.getModifyPassword()));
    }
}
