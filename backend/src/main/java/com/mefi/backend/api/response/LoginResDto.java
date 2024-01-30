package com.mefi.backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResDto {
    
    // 이메일
    private String emial;

    // 이름
    private String name;

    // 부서
    private String dept;

    // 직책
    private String position;
}
