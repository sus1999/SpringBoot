package com.global.account;

import com.global.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/*
Spring Security 에서 제공하는 user 정보와
우리가 만든 domain 에서 다루는 user 정보 사이에서
adapter 역할을 하는 클래스
  ㄴ UserAccount : Principal 객체로 사용함
*/
@Getter
public class UserAccount extends User {
  private Account account;
  public UserAccount(Account account){
    super(account.getNickName(),
          account.getPassword(),
          List.of(new SimpleGrantedAuthority("ROLE_USER")));
    this.account = account;
  }

}
